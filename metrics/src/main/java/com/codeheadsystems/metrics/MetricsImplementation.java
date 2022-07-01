// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

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
