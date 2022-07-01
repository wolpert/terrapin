// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.metrics;

import java.util.function.Supplier;

/**
 * Empty class that does nothing.
 */
public class NullMetricsImplementation implements MetricsImplementation {

    @Override
    public void count(final String name, final long value) {

    }

    @Override
    public <R> R time(final String name, final Supplier<R> supplier) {
        return supplier.get();
    }

    @Override
    public void close() {

    }
}
