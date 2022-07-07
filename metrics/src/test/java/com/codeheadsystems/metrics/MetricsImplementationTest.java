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

import static com.codeheadsystems.metrics.MetricsImplementation.FAIL;
import static com.codeheadsystems.metrics.MetricsImplementation.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsImplementationTest {

    private static final String METRIC_NAME = "name";
    private static final long ONE = 1;
    public static final int ZERO = 0;

    @Mock private MetricsVendor metricsVendor;
    @Mock private Supplier<Boolean> supplier;

    private MetricsImplementation metricsImplementation;

    @BeforeEach
    void setUp() {
        metricsImplementation = new MetricsImplementation(metricsVendor);
    }

    @Test
    void count() throws IOException {
        metricsImplementation.count(METRIC_NAME, ONE);
        metricsImplementation.close();

        verify(metricsVendor).count(METRIC_NAME, Map.of(), ONE);
    }

    @Test
    void latency_success() throws IOException {
        when(metricsVendor.time(METRIC_NAME, Map.of(), supplier))
                .thenReturn(true);

        assertThat(metricsImplementation.time(METRIC_NAME, supplier))
                .isTrue();
        metricsImplementation.close();

        verify(metricsVendor).count(METRIC_NAME + SUCCESS, Map.of(), ONE);
        verify(metricsVendor).count(METRIC_NAME + FAIL, Map.of(), ZERO);
    }

    @Test
    void latency_fail() throws IOException {
        when(metricsVendor.time(METRIC_NAME, Map.of(), supplier))
                .thenThrow(new IllegalArgumentException("mock"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> metricsImplementation.time(METRIC_NAME, supplier))
                .withMessage("mock");

        metricsImplementation.close();

        verify(metricsVendor).count(METRIC_NAME + SUCCESS, Map.of(), ZERO);
        verify(metricsVendor).count(METRIC_NAME + FAIL, Map.of(), ONE);
    }

    @Test
    void close() throws IOException {
        metricsImplementation.close();
    }

}