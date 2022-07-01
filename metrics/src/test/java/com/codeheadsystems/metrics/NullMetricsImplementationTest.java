// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NullMetricsImplementationTest {

    public static final String METRIC_NAME = "metric name";
    public static final long ONE = 1;
    MetricsImplementation metricsImplementation;

    @BeforeEach
    void setup() {
        metricsImplementation = new NullMetricsImplementation();
    }

    @Test
    void count() {
        metricsImplementation.count(METRIC_NAME, ONE);
    }

    @Test
    void time() {
        assertThat(metricsImplementation.time(METRIC_NAME, () -> Boolean.TRUE))
                .isTrue();
    }

    @Test
    void close() throws IOException {
        metricsImplementation.close(); // no change.
    }
}