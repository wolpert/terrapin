/*
 *    Copyright (c) 2022 Ned Wolpert <ned.wolpert@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.codeheadsystems.keystore.server.dao.ddb.dagger;

import com.codeheadsystems.keystore.server.dao.KeyDao;
import com.codeheadsystems.keystore.server.dao.KeyDaoDynamoDb;
import com.codeheadsystems.keystore.server.dao.ddb.configuration.ImmutableTableConfiguration;
import com.codeheadsystems.keystore.server.dao.ddb.configuration.TableConfiguration;
import com.codeheadsystems.keystore.server.dao.ddb.factory.DdbObjectMapperFactory;
import com.codeheadsystems.keystore.server.exception.RetryableException;
import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.dagger.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Basic DDB module.
 * Use this to create a KeyDAO. You'll need the DynamoDBClient.
 * TODO add a circuit breaker to the retry.
 */
@Module(includes = {DdbModule.Binder.class, MetricsModule.class})
public class DdbModule {

  public static final String DDB_DAO_RETRY = "DDB_DAO_RETRY";

  @Provides
  @Singleton
  public ObjectMapper objectMapper(final DdbObjectMapperFactory factory) {
    return factory.generate();
  }

  @Provides
  @Singleton
  public TableConfiguration tableConfiguration() {
    return ImmutableTableConfiguration.builder().build();
  }

  /**
   * Provides a retry object for dynamodb.
   *
   * @param metrics system we are using.
   * @return retry.
   */
  @Named(DDB_DAO_RETRY)
  @Provides
  @Singleton
  public Retry retry(final Metrics metrics) {
    final RetryConfig config = RetryConfig.custom()
        .maxAttempts(3)
        .retryExceptions(RetryableException.class)
        .intervalFunction(IntervalFunction.ofExponentialBackoff(100, 2))
        .failAfterMaxAttempts(true)
        .build();
    final RetryRegistry registry = RetryRegistry.of(config);
    TaggedRetryMetrics.ofRetryRegistry(registry)
        .bindTo(metrics.registry());
    return registry.retry(DDB_DAO_RETRY);
  }

  /**
   * Binder to create the dao.
   */
  @Module
  public interface Binder {

    @Binds
    KeyDao dao(KeyDaoDynamoDb dao);

  }

}
