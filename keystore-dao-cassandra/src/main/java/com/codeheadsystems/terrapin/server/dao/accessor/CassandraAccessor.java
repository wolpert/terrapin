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

package com.codeheadsystems.terrapin.server.dao.accessor;

import static com.codeheadsystems.terrapin.server.dao.dagger.CassandraModule.CASSANDRA_RETRY;

import com.codeheadsystems.metrics.Metrics;
import com.datastax.oss.driver.api.core.CqlSession;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CassandraAccessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraAccessor.class);

    private final CqlSession session;
    private final Metrics metrics;

    @Inject
    public CassandraAccessor(final CqlSession session,
                             final Metrics metrics,
                             @Named(CASSANDRA_RETRY) final Retry retry) {
        LOGGER.info("CassandraAccessor({},{},{})", session, metrics, retry);
        this.session = session;
        this.metrics = metrics;
    }

    private <T> T call(final String metricName,
                       final Supplier<T> supplier) {
        final Timer timer = metrics.registry().timer(metricName);
        return metrics.time(metricName, timer, supplier);
    }
}
