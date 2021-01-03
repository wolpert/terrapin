package com.codeheadsystems.statemachine.manager;

import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Use this metric manager to avoid all internal metrics.
 */
@Singleton
public class NullMetricManager implements MetricManager {

    @Inject
    public NullMetricManager() {

    }

    @Override public void meter(final String metricName,
                                final long value) {
        // empty.
    }

    @Override public <R> R time(final String metricName, final Supplier<R> supplier) {
        return supplier.get();
    }
}
