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

    private final Map<String, String> dimensions;
    private final HashMap<String, Long> counts;

    public MetricsImplementation(final MetricsVendor metricsVendor) {
        this.metricsVendor = metricsVendor;
        this.dimensions = new HashMap<>(); // This is not immutable.
        this.counts = new HashMap<>();
    }

    @Override
    public void setDimensions(final Map<String, String> dimensions) {
        dimensions.clear();
        addDimensions(dimensions);
    }

    @Override
    public void addDimensions(final Map<String, String> dimensions) {
        this.dimensions.putAll(dimensions);
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

    public <R> R time(final String name,
                      final Supplier<R> supplier) {
        count(name + SUCCESS, 0);
        count(name + FAIL, 0);
        try {
            final R result = metricsVendor.time(name, dimensions, supplier);
            count(name + SUCCESS, 1);
            return result;
        } catch (RuntimeException re) {
            count(name + FAIL, 1);
            throw re;
        }
    }

    @Override
    public void close() throws IOException {
        counts.forEach((name, value) -> metricsVendor.count(name, dimensions, value));
        dimensions.clear();
        counts.clear();
    }

    // ---- Taken from dropwizard metric registry.

    /**
     * Concatenates elements to form a dotted name, eliding any null values or empty strings.
     *
     * @param name  the first element of the name
     * @param names the remaining elements of the name
     * @return {@code name} and {@code names} concatenated by periods
     */
    public String name(final String name, final String... names) {
        final StringBuilder builder = new StringBuilder();
        append(builder, name);
        if (names != null) {
            for (String s : names) {
                append(builder, s);
            }
        }
        return builder.toString();
    }

    /**
     * Concatenates a class name and elements to form a dotted name, eliding any null values or
     * empty strings.
     *
     * @param klass the first element of the name
     * @param names the remaining elements of the name
     * @return {@code klass} and {@code names} concatenated by periods
     */
    public String name(Class<?> klass, String... names) {
        return name(klass.getName(), names);
    }

    private void append(StringBuilder builder, String part) {
        if (part != null && !part.isEmpty()) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(part);
        }
    }
}
