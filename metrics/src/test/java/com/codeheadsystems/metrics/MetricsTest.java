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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsTest {

    private static final String SUCCESS = "success";
    private static final String FAIL = "fail";
    private static final String METRIC_NAME = "name";
    private static final long ONE = 1;
    public static final int ZERO = 0;

    @Mock private MetricsImplementation metricsImplementation;
    @Mock private Supplier<Boolean> supplier;

    private Metrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new Metrics(metricsImplementation, SUCCESS, FAIL);
    }

    @Test
    void count() throws IOException {
        metrics.count(METRIC_NAME, ONE);
        metrics.close();

        verify(metricsImplementation).count(METRIC_NAME, ONE);
    }

    @Test
    void latency_success() throws IOException {
        when(metricsImplementation.time(METRIC_NAME, supplier))
                .thenReturn(true);

        assertThat(metrics.latency(METRIC_NAME, supplier))
                .isTrue();
        metrics.close();

        verify(metricsImplementation).count(METRIC_NAME + SUCCESS, ONE);
        verify(metricsImplementation).count(METRIC_NAME + FAIL, ZERO);
    }

    @Test
    void latency_fail() throws IOException {
        when(metricsImplementation.time(METRIC_NAME, supplier))
                .thenThrow(new IllegalArgumentException("mock"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> metrics.latency(METRIC_NAME, supplier))
                .withMessage("mock");

        metrics.close();

        verify(metricsImplementation).count(METRIC_NAME + SUCCESS, ZERO);
        verify(metricsImplementation).count(METRIC_NAME + FAIL, ONE);
    }

    @Test
    void close() throws IOException {
        metrics.close();
    }

}