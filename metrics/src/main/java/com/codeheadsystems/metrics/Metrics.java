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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class handles the metrics we need. It uses a metrics implementation object to send metrics to the server.
 * Basically it's a helper.
 */
public class Metrics implements Closeable {

    private final MetricsImplementation metricsImplementation;
    private final String successCountName;
    private final String failCountName;
    private final Map<String, Long> counts = new HashMap<>();

    public Metrics(final MetricsImplementation metricsImplementation,
                   final String successCountName,
                   final String failCountName) {
        this.metricsImplementation = metricsImplementation;
        this.successCountName = successCountName;
        this.failCountName = failCountName;
    }

    public void count(final String name,
                      final long value) {
        final long currentValue = counts.containsKey(name) ? counts.get(name) : 0;
        counts.put(name, currentValue + value);
    }

    public <R> R latency(final String name,
                         final Supplier<R> supplier) {
        try {
            final R result = metricsImplementation.time(name, supplier);
            count(name + successCountName, 1);
            count(name + failCountName, 0);
            return result;
        } catch (RuntimeException re) {
            count(name + successCountName, 0);
            count(name + failCountName, 1);
            throw re;
        }
    }

    @Override
    public void close() throws IOException {
        for(Map.Entry<String, Long> entry : counts.entrySet()) {
            metricsImplementation.count(entry.getKey(), entry.getValue());
        }
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
