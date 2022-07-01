// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.metrics;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsFactory.class);

    private final MetricsImplementationFactory metricsImplementationFactory;
    private final String successName;
    private final String failName;

    public MetricsFactory(final MetricsImplementationFactory metricsImplementationFactory,
                          final String successName,
                          final String failName) {
        this.metricsImplementationFactory = metricsImplementationFactory;
        this.successName = successName;
        this.failName = failName;
        LOGGER.info("MetricsFactory({},{},{})", this.metricsImplementationFactory, this.successName, this.failName);
    }

    public Metrics get() {
        return new Metrics(metricsImplementationFactory.get(), successName, failName);
    }

    public void with(final Consumer<Metrics> consumer) {
        with(m -> {
            consumer.accept(m);
            return null;
        });
    }

    public <R> R with(final Function<Metrics,R> function) {
        try(Metrics metrics = get()) {
            return function.apply(metrics);
        } catch (IOException e) {
            LOGGER.error("Metrics Fail", e);
            throw new IllegalStateException("Metrics fail", e);
        }
    }
}
