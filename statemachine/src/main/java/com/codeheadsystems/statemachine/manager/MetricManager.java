package com.codeheadsystems.statemachine.manager;

import java.util.function.Supplier;

/**
 * Provide your own metric manager.
 */
public interface MetricManager {

    /**
     * This is effectively a 'rate'. Number/interval.
     *
     * @param metricName to use.
     * @param value a value. Note, having a zero here is useful for cases
     *              like error rates to indicate no value.
     */
    void meter(String metricName, long value);

    /**
     * Calls the time method via a runable instead of a supplier.
     *
     * @param metricName to use.
     * @param runnable to execute.
     * @return basically nothing.
     */
    default Void time(String metricName, Runnable runnable) {
        return time(metricName, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Latency of a given method. Note this should include counts too.
     *
     * @param metricName to use.
     * @param supplier to execute.
     * @param <R> return type.
     * @return the value from the method.
     */
    <R> R time(String metricName, Supplier<R> supplier);
}
