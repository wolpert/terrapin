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

package com.codeheadsystems.terrapin.server.dao.dagger;

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.dagger.MetricsModule;
import com.codeheadsystems.terrapin.server.dao.DdbObjectMapperFactory;
import com.codeheadsystems.terrapin.server.dao.ImmutableTableConfiguration;
import com.codeheadsystems.terrapin.server.dao.KeyDAO;
import com.codeheadsystems.terrapin.server.dao.KeyDAODynamoDB;
import com.codeheadsystems.terrapin.server.dao.TableConfiguration;
import com.codeheadsystems.terrapin.server.exception.RetryableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import javax.inject.Named;
import javax.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Basic DDB module.
 * Use this to create a KeyDAO. You'll need the DynamoDBClient.
 * <p>
 * TODO add a circuit breaker to the retry.
 */
@Module(includes = {DDBModule.Binder.class, MetricsModule.class})
public class DDBModule {

  public static final String DDB_DAO_RETRY = "DDB_DAO_RETRY";
  private final DynamoDbClient client;
  private final TableConfiguration tableConfiguration;

  public DDBModule() {
    this(DynamoDbClient.create());
  }

  public DDBModule(final DynamoDbClient dynamoDbClient) {
    this(dynamoDbClient, ImmutableTableConfiguration.builder().build());
  }

  public DDBModule(final DynamoDbClient dynamoDbClient,
                   final TableConfiguration tableConfiguration) {
    this.client = dynamoDbClient;
    this.tableConfiguration = tableConfiguration;
  }

  @Provides
  @Singleton
  public ObjectMapper objectMapper(final DdbObjectMapperFactory factory) {
    return factory.generate();
  }

  @Provides
  @Singleton
  public DynamoDbClient dynamoDbClient() {
    return client;
  }

  @Provides
  @Singleton
  public TableConfiguration tableConfiguration() {
    return tableConfiguration;
  }

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

  @Module
  public interface Binder {

    @Binds
    KeyDAO dao(KeyDAODynamoDB dao);

  }

}
