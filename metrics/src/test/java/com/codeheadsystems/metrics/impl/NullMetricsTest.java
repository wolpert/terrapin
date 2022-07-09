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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Test nothing happens :/
class NullMetricsTest {

    public static final String NAME = "name";
    public static final String VALUE = "value";
    private NullMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new NullMetrics();
    }

    @Test
    void close() throws IOException {
        metrics.close();
    }

    @Test
    void setDimensions() {
        metrics.setDimensions(Map.of());
    }

    @Test
    void addDimensions() {
        metrics.addDimensions(Map.of());
    }

    @Test
    void addDimension() {
        metrics.addDimension(NAME, VALUE);
    }

    @Test
    void count() {
        metrics.count(NAME, 1);
    }

    @Test
    void time() {
        final Supplier<Boolean> supplier = () -> Boolean.TRUE;
        final Boolean result = metrics.time(NAME, supplier);
        assertThat(result)
                .isNotNull()
                .isTrue();
    }

    @Test
    void name() {
        assertThat(metrics.name(NullMetricsTest.class, "one", "two"))
                .isEqualTo("com.codeheadsystems.metrics.impl.NullMetricsTest.one.two");
    }
}