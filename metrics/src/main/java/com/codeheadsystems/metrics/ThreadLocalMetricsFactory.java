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

package com.codeheadsystems.metrics;

import java.io.IOException;
import java.util.function.Consumer;
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
public class ThreadLocalMetricsFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalMetricsFactory.class);

    private final MetricsFactory metricsFactory;
    private final ThreadLocal<Metrics> threadLocalMetrics = ThreadLocal.withInitial(MetricsFactory::nullMetrics);

    @Inject
    public ThreadLocalMetricsFactory(final MetricsFactory metricsFactory) {
        LOGGER.info("ThreadLocalMetricsFactory({})", metricsFactory);
        this.metricsFactory = metricsFactory;
    }

    public void with(final Runnable runnable) {
        with(m-> {
            runnable.run();
        });
    }

    public void with(final Consumer<Metrics> consumer) {
        with(m -> {
            consumer.accept(m);
            return null;
        });
    }

    public <R> R with(final Function<Metrics, R> function) {
        try (Metrics metrics = metricsFactory.get()) {
            threadLocalMetrics.set(metrics);
            return function.apply(metrics);
        } catch (IOException e) {
            LOGGER.error("Metrics Fail", e);
            throw new IllegalStateException("Metrics fail", e);
        } finally {
            threadLocalMetrics.set(MetricsFactory.nullMetrics());
        }
    }

    public Metrics get() {
        return threadLocalMetrics.get();
    }

}
