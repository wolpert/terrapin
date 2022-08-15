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

package com.codeheadsystems.keystore.server.dao.casssandra.accessor;

import com.codeheadsystems.keystore.server.dao.casssandra.dagger.CassandraModule;
import com.codeheadsystems.metrics.Metrics;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.Timer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a wrapper to the cassandra driver. It only exists to manage retries and metrics.
 */
@Singleton
public class CassandraAccessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraAccessor.class);
  private static final String EXECUTE_STATEMENT = "executeStatement";

  private final CqlSession session;
  private final Metrics metrics;

  private final Function<Statement<?>, ResultSet> executeStatement;

  /**
   * Default constructor.
   *
   * @param session the base CQL Session.
   * @param metrics for reporting.
   * @param retry retry policy.
   */
  @Inject
  public CassandraAccessor(final CqlSession session,
                           final Metrics metrics,
                           @Named(CassandraModule.CASSANDRA_RETRY) final Retry retry) {
    LOGGER.info("CassandraAccessor({},{},{})", session, metrics, retry);
    this.session = session;
    this.metrics = metrics;
    this.executeStatement = Retry.decorateFunction(retry,                  // retries
        (request) -> call(EXECUTE_STATEMENT,     // exception check and metrics
            () -> session.execute(request))); // the actual function
  }

  public ResultSet execute(final Statement statement) {
    return executeStatement.apply(statement);
  }


  private <T> T call(final String metricName,
                     final Supplier<T> supplier) {
    final Timer timer = metrics.registry().timer(metricName);
    return metrics.time(metricName, timer, supplier);
  }
}
