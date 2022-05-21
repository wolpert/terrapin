package com.codeheadsystems.statemachine.manager.impls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.codeheadsystems.statemachine.manager.MetricManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NullMetricManagerTest {

    private static final String METRIC_NAME = "metricName";

    private MetricManager metricManager;

    @BeforeEach
    void setUp() {
        metricManager = new NullMetricManager();
    }

    @Test
    void meter() {
        metricManager.meter(METRIC_NAME, 5);
        // nothing to assert
    }

    @Test
    void time() {
        final Boolean b = metricManager.time(METRIC_NAME, ()->Boolean.TRUE);
        assertThat(b).isTrue();
    }
}