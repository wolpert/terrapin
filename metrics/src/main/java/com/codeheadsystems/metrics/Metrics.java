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

import static com.codeheadsystems.metrics.dagger.MetricsModule.METER_REGISTRY;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Provides helper methods for micrometer metrics.
 */
@Singleton
public class Metrics {

    private final MeterRegistry registry;

    @Inject
    public Metrics(@Named(METER_REGISTRY) final MeterRegistry registry) {
        this.registry = registry;
    }

    public MeterRegistry registry() {
        return registry;
    }

    /**
     * Helper method to time a request and include the success counters.
     */
    public <R> R time(final String name,
                      final Timer timer,
                      final Supplier<R> supplier) {
        final Counter success = registry.counter(name, "success", "true");
        final Counter failure = registry.counter(name, "success", "false");
        try {
            final R result = timer.record(supplier);
            success.increment(1);
            failure.increment(0);
            return result;
        } catch (RuntimeException re) {
            success.increment(0);
            failure.increment(1);
            throw re;
        }
    }

}