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

package com.codeheadsystems.metrics.helper;

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.impl.MetricsFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PoolableThreadLocalMetricsHelper extends ThreadLocalMetricsHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolableThreadLocalMetricsHelper.class);

    private final ArrayList<Metrics> pool;

    @Inject
    public PoolableThreadLocalMetricsHelper(final MetricsFactory metricsFactory) {
        super(metricsFactory);
        pool = new ArrayList<>();
        LOGGER.info("PoolableThreadLocalMetricsHelper({})", metricsFactory);
    }

    public int poolSize() {
        return pool.size();
    }

    private Optional<Metrics> getFromPool() {
        synchronized (pool) {
            if (pool.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(pool.remove(0));
            }
        }
    }

    private void putIntoPool(final Metrics metrics) {
        synchronized (pool) {
            pool.add(metrics);
        }
    }

    @Override
    protected Metrics internalGetMetrics() {
        return getFromPool().orElseGet(super::internalGetMetrics);
    }

    @Override
    protected void internalMetricsCleanup(final Metrics metrics) {
        try {
            metrics.close();
            putIntoPool(metrics);
        } catch (IOException e) {
            LOGGER.error("Metrics ignored due to metrics failure: {}", metrics, e);
        }
    }
}
