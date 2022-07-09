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
import com.codeheadsystems.metrics.MetricsHelper;
import com.codeheadsystems.metrics.impl.MetricsFactory;
import java.io.IOException;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory is a proxy for the real metrics factory. It allows for the setting/getting to be done in one place so
 * it is usable in children.
 */
@Singleton
public class ThreadLocalMetricsHelper implements MetricsHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalMetricsHelper.class);

    private final MetricsFactory metricsFactory;
    private final ThreadLocal<Metrics> threadLocalMetrics;

    @Inject
    public ThreadLocalMetricsHelper(final MetricsFactory metricsFactory) {
        LOGGER.info("ThreadLocalMetricsFactory({})", metricsFactory);
        this.metricsFactory = metricsFactory;
        this.threadLocalMetrics = ThreadLocal.withInitial(metricsFactory::nullMetrics);
    }

    protected Metrics internalGetMetrics() {
        return metricsFactory.get();
    }

    protected void internalMetricsCleanup(final Metrics metrics) {
        try {
            metrics.close();
        } catch (IOException e) {
            LOGGER.error("Metrics ignored due to metrics failure: {}", metrics, e);
        }
    }

    @Override
    public <R> R with(final Function<Metrics, R> function) {
        final Metrics metrics = internalGetMetrics();
        try {
            threadLocalMetrics.set(metrics);
            return function.apply(metrics);
        } finally {
            internalMetricsCleanup(metrics);
            threadLocalMetrics.set(metricsFactory.nullMetrics());
        }
    }

    @Override
    public Metrics get() {
        final Metrics metrics = threadLocalMetrics.get();
        LOGGER.debug("get()-> {}", metrics);
        return metrics;
    }

}
