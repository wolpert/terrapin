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
import java.util.function.Supplier;

/**
 * Base metrics we support. Any metric service needs to implement these metrics.
 */
public interface MetricsImplementation extends Closeable {

    /**
     * Counts the value into the metric. Can be any positive/negative number including zero.
     * Note, in dropwizard metrics, this is likely just a histogram.
     *
     * @param name of the metric.
     * @param value for the counter.
     */
    void count(String name, long value);

    /**
     * Default latency check. Note that this does not automatically track exceptions.
     *
     * @param name of the metric.
     * @param supplier function to call. Should return a value.
     * @param <R> return type.
     * @return a value.
     */
    <R> R time(String name, Supplier<R> supplier);

}
