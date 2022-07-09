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

package com.codeheadsystems.metrics.dropwizard;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DropwizardMetricsVendorTest {

    private static final String NAME = "name";
    private static final Map<String, String> DIMENSIONS = Map.of();

    @Mock private MetricRegistry registry;
    @Mock private Meter meter;
    @Mock private Timer timer;

    private DropwizardMetricsVendor vendor;

    @BeforeEach
    void setUp() {
        vendor = new DropwizardMetricsVendor(registry);
    }

    @Test
    void count() {
        when(registry.meter(NAME)).thenReturn(meter);

        vendor.count(NAME, DIMENSIONS, 10);

        verify(meter).mark(10);
    }

    @Test
    void time() {
        when(registry.timer(NAME)).thenReturn(timer);

        vendor.time(NAME, DIMENSIONS, 100);

        verify(timer).update(100, TimeUnit.MILLISECONDS);
    }

    @Test
    void close() throws IOException {
        vendor.close(); // nothing happens...
    }
}