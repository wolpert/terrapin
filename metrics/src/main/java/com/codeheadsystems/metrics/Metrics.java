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
import java.util.Map;
import java.util.function.Supplier;

/**
 * Metrics are designed to NOT report metrics until the object itself is closed. You MUST close
 * the metrics object.
 */
public interface Metrics extends Closeable {

    /**
     * You can set the dimensions for a metric. It is valid as long as it is open. We reset the
     * dimensions when the metric is closed. It is optional to use. If you use this method, we
     * clear out the old dimensions and replace them with this map.
     *
     * @param dimensions
     */
    void setDimensions(Map<String, String> dimensions);

    /**
     * This adds the dimensions to the existing set. Note, if an entry is already listed, it will
     * overwrite the dimensions.
     *
     * @param dimensions
     */
    void addDimensions(Map<String, String> dimensions);

    void addDimension(String dimensionName, String dimensionValue);

    /**
     * Counts the value into the metric. Can be any positive/negative number including zero.
     * Note, in dropwizard metrics, this is likely just a histogram. Note, if the metrics
     * implementation does not support dimensions they will be ignored.
     *
     * @param name  of the metric.
     * @param value for the counter.
     */
    void count(String name, long value);

    /**
     * Default latency check. Note that this does not automatically track exceptions.Note, if the metrics
     * * implementation does not support dimensions they will be ignored.
     *
     * @param name     of the metric.
     * @param supplier function to call. Should return a value.
     * @param <R>      return type.
     * @return a value.
     */
    <R> R time(String name, Supplier<R> supplier);

    /**
     * Concatenates a class name and elements to form a dotted name, eliding any null values or
     * empty strings.
     *
     * @param klass the first element of the name
     * @param names the remaining elements of the name
     * @return {@code klass} and {@code names} concatenated by periods
     */
    default String name(Class<?> klass, String... names) {
        return name(klass.getName(), names);
    }

    /**
     * Concatenates elements to form a dotted name, eliding any null values or empty strings.
     *
     * @param name  the first element of the name
     * @param names the remaining elements of the name
     * @return {@code name} and {@code names} concatenated by periods
     */
    default String name(final String name, final String... names) {
        final StringBuilder builder = new StringBuilder(name);
        if (names != null) {
            for (String s : names) {
                if (s != null && !s.isEmpty()) {
                    builder.append('.');
                    builder.append(s);
                }
            }
        }
        return builder.toString();
    }

}
