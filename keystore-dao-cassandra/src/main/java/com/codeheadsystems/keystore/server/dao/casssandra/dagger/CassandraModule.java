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

package com.codeheadsystems.keystore.server.dao.casssandra.dagger;

import com.codeheadsystems.keystore.server.dao.CassandraKeyDao;
import com.codeheadsystems.keystore.server.dao.KeyDao;
import com.codeheadsystems.keystore.server.dao.casssandra.configuration.ImmutableTableConfiguration;
import com.codeheadsystems.keystore.server.dao.casssandra.configuration.TableConfiguration;
import com.codeheadsystems.keystore.server.exception.RetryableException;
import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.dagger.MetricsModule;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Clock;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Builds out the cassandra module.
 */
@Module(includes = {
    CassandraModule.Binder.class,
    CqlSessionModule.class,
    MetricsModule.class,
    StatementModule.class
})
public class CassandraModule {

  public static final String CASSANDRA_RETRY = "CASSANDRA_RETRY";

  private final TableConfiguration tableConfiguration;

  /**
   * Uses the default table configuration.
   */
  public CassandraModule() {
    this(ImmutableTableConfiguration.builder().build());
  }

  /**
   * Uses the given table configuration.
   *
   * @param tableConfiguration configuration.
   */
  public CassandraModule(final TableConfiguration tableConfiguration) {
    this.tableConfiguration = tableConfiguration;
  }

  @Provides
  @Singleton
  public TableConfiguration tableConfiguration() {
    return tableConfiguration;
  }

  /**
   * Provides a retry policy given the metrics.
   *
   * @param metrics object in play.
   * @return a retry object specific for Cassandra.
   */
  @Named(CASSANDRA_RETRY)
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
    return registry.retry(CASSANDRA_RETRY);
  }

  @Provides
  @Singleton
  public Clock clock() {
    return Clock.systemUTC();
  }

  /**
   * Exposes the Cassandra dao as the DAO to use.
   */
  @Module
  public interface Binder {

    @Binds
    KeyDao dao(CassandraKeyDao dao);

  }
}
