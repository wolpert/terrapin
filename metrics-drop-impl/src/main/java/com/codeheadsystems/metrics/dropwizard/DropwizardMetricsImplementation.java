// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codeheadsystems.metrics.MetricsImplementation;
import java.io.IOException;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Currently dimensions are ignored for dropwizard.
 */
@Singleton
public class DropwizardMetricsImplementation implements MetricsImplementation {

    private final MetricRegistry metricRegistry;

    @Inject
    public DropwizardMetricsImplementation(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void count(final String name, final long value) {
        metricRegistry.histogram(name).update(value);
    }

    @Override
    public <R> R time(final String name, final Supplier<R> supplier) {
        return metricRegistry.timer(name).timeSupplier(supplier);
    }

    @Override
    public void close() throws IOException {

    }
}
