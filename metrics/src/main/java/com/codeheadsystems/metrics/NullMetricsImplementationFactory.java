// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.metrics;

public class NullMetricsImplementationFactory implements MetricsImplementationFactory {

    private static final MetricsImplementation METRICS_IMPLEMENTATION = new NullMetricsImplementation();

    @Override
    public MetricsImplementation get() {
        return METRICS_IMPLEMENTATION;
    }
}
