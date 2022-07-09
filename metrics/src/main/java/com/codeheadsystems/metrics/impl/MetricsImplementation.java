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

package com.codeheadsystems.metrics.impl;

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.vendor.MetricsVendor;
import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class handles the metrics we need. It uses a metrics implementation object to send metrics to the server.
 * Basically it's a helper.
 */
public class MetricsImplementation implements Metrics {

    public static final String SUCCESS = ".success";
    public static final String FAIL = ".fail";

    private final MetricsVendor metricsVendor;
    private final Clock clock;
    private final Map<String, String> dimensions;
    private final HashMap<String, Long> counts;
    private final HashMap<String, Long> times;

    public MetricsImplementation(final MetricsVendor metricsVendor,
                                 final Clock clock) {
        this.metricsVendor = metricsVendor;
        this.clock = clock;
        this.dimensions = new HashMap<>(); // This is not immutable.
        this.counts = new HashMap<>();
        this.times = new HashMap<>();
    }

    @Override
    public void setDimensions(final Map<String, String> dimensionsToUse) {
        this.dimensions.clear();
        addDimensions(dimensionsToUse);
    }

    @Override
    public void addDimensions(final Map<String, String> dimensionsToUse) {
        this.dimensions.putAll(dimensionsToUse);
    }

    @Override
    public void addDimension(final String dimensionName, final String dimensionValue) {
        this.dimensions.put(dimensionName, dimensionValue);
    }

    public void count(final String name,
                      final long value) {
        long currentValue = counts.getOrDefault(name, 0L);
        counts.put(name, currentValue + value);
    }

    /**
     * TODO: Make this not report the metrics until they are closed.
     */
    public <R> R time(final String name,
                      final Supplier<R> supplier) {
        count(name + SUCCESS, 0);
        count(name + FAIL, 0);
        final long startTime = clock.millis();
        try {
            final R result = supplier.get();
            count(name + SUCCESS, 1);
            return result;
        } catch (RuntimeException re) {
            count(name + FAIL, 1);
            throw re;
        } finally {
            final long endTime = clock.millis();
            times.put(name, endTime - startTime);
        }
    }

    @Override
    public void close() throws IOException {
        counts.forEach((name, value) -> metricsVendor.count(name, dimensions, value));
        times.forEach((name, value) -> metricsVendor.time(name, dimensions, value));
        dimensions.clear();
        counts.clear();
    }

}
