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

package com.codeheadsystems.metrics.impl;

import static com.codeheadsystems.metrics.impl.MetricsImplementation.FAIL;
import static com.codeheadsystems.metrics.impl.MetricsImplementation.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.metrics.impl.MetricsImplementation;
import com.codeheadsystems.metrics.vendor.MetricsVendor;
import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsImplementationTest {

    public static final Map<String, String> EMPTY_DIMENSIONS = Map.of();
    public static final Map<String, String> ONE_DIMENSIONS = Map.of("x", "y");
    public static final Map<String, String> TWO_DIMENSIONS = Map.of("a", "b", "c", "d");
    public static final String DIMENSION_NAME = "dn";
    public static final String DIMENSION_VALUE = "dv";
    public static final int ZERO = 0;
    private static final String METRIC_NAME = "name";
    private static final long ONE = 1;
    private TestVendor testVendor;
    @Mock private MetricsVendor metricsVendor;
    @Mock private Supplier<Boolean> supplier;
    @Mock private Clock clock;
    @Captor private ArgumentCaptor<Map<String, String>> dimensionCapture;

    private MetricsImplementation metricsImplementation;

    @Test
    void count() throws IOException {
        testVendor = new TestVendor();
        metricsImplementation = new MetricsImplementation(testVendor, clock);
        metricsImplementation.count(METRIC_NAME, ONE);
        metricsImplementation.close();

        assertThat(testVendor)
                .hasFieldOrPropertyWithValue("countName", METRIC_NAME)
                .hasFieldOrPropertyWithValue("countDimensions", EMPTY_DIMENSIONS)
                .hasFieldOrPropertyWithValue("countValue", ONE);
    }

    @Test
    void count_with_dimensions() throws IOException {
        testVendor = new TestVendor();
        metricsImplementation = new MetricsImplementation(testVendor, clock);
        metricsImplementation.setDimensions(TWO_DIMENSIONS);

        metricsImplementation.count(METRIC_NAME, ONE);
        metricsImplementation.close();

        assertThat(testVendor)
                .hasFieldOrPropertyWithValue("countName", METRIC_NAME)
                .hasFieldOrPropertyWithValue("countDimensions", TWO_DIMENSIONS)
                .hasFieldOrPropertyWithValue("countValue", ONE);
        // without setting dimensions the second time
        testVendor.reset();

        metricsImplementation.count(METRIC_NAME, ONE);
        metricsImplementation.close();

        assertThat(testVendor)
                .hasFieldOrPropertyWithValue("countName", METRIC_NAME)
                .hasFieldOrPropertyWithValue("countDimensions", EMPTY_DIMENSIONS)
                .hasFieldOrPropertyWithValue("countValue", ONE);

    }

    @Test
    void count_with_added_dimensions() throws IOException {
        testVendor = new TestVendor();
        metricsImplementation = new MetricsImplementation(testVendor, clock);
        metricsImplementation.setDimensions(TWO_DIMENSIONS);
        metricsImplementation.addDimensions(ONE_DIMENSIONS);
        metricsImplementation.addDimension(DIMENSION_NAME, DIMENSION_VALUE);

        metricsImplementation.count(METRIC_NAME, ONE);
        metricsImplementation.close();

        final HashMap<String,String> expectedDimensions = new HashMap<>(TWO_DIMENSIONS);
        expectedDimensions.putAll(ONE_DIMENSIONS);
        expectedDimensions.put(DIMENSION_NAME, DIMENSION_VALUE);

        assertThat(testVendor)
                .hasFieldOrPropertyWithValue("countName", METRIC_NAME)
                .hasFieldOrPropertyWithValue("countDimensions", expectedDimensions)
                .hasFieldOrPropertyWithValue("countValue", ONE);
    }

    @Test
    void latency_success() throws IOException {
        metricsImplementation = new MetricsImplementation(metricsVendor, clock);
        when(supplier.get())
                .thenReturn(true);
        when(clock.millis()).thenReturn(100L, 120L);

        assertThat(metricsImplementation.time(METRIC_NAME, supplier))
                .isTrue();
        metricsImplementation.close();

        verify(metricsVendor).count(METRIC_NAME + SUCCESS, EMPTY_DIMENSIONS, ONE);
        verify(metricsVendor).count(METRIC_NAME + FAIL, EMPTY_DIMENSIONS, ZERO);
        verify(metricsVendor).time(METRIC_NAME, EMPTY_DIMENSIONS, 20L);
    }

    @Test
    void latency_fail() throws IOException {
        metricsImplementation = new MetricsImplementation(metricsVendor, clock);
        when(supplier.get())
                .thenThrow(new IllegalArgumentException("mock"));
        when(clock.millis()).thenReturn(100L, 120L);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> metricsImplementation.time(METRIC_NAME, supplier))
                .withMessage("mock");

        metricsImplementation.close();

        verify(metricsVendor).count(METRIC_NAME + SUCCESS, EMPTY_DIMENSIONS, ZERO);
        verify(metricsVendor).count(METRIC_NAME + FAIL, EMPTY_DIMENSIONS, ONE);
        verify(metricsVendor).time(METRIC_NAME, EMPTY_DIMENSIONS, 20L);
    }

    @Test
    void close() throws IOException {
        metricsImplementation = new MetricsImplementation(metricsVendor, clock);
        metricsImplementation.close();
    }

    class TestVendor implements MetricsVendor {
        private String countName;
        private Map<String, String> countDimensions;
        private Long countValue;
        private String timeName;
        private Map<String, String> timeDimensions;
        private Long timeValue;

        public void reset() {
            countName = null;
            countDimensions = null;
            countValue = null;
            timeName = null;
            timeDimensions = null;
            timeValue = null;
        }

        public String getCountName() {
            return countName;
        }

        public Map<String, String> getCountDimensions() {
            return countDimensions;
        }

        public Long getCountValue() {
            return countValue;
        }

        public String getTimeName() {
            return timeName;
        }

        public Map<String, String> getTimeDimensions() {
            return timeDimensions;
        }

        public Long getTimeValue() {
            return timeValue;
        }

        @Override
        public void count(final String name, final Map<String, String> dimensions, final long value) {
            countName = name;
            countDimensions = new HashMap<>(dimensions);
            countValue = value;
        }

        @Override
        public void time(final String name, final Map<String, String> dimensions, final long value) {
            timeName = name;
            timeDimensions = new HashMap<>(dimensions);
            timeValue = value;
        }

        @Override
        public void close() throws IOException {
        }
    }

}