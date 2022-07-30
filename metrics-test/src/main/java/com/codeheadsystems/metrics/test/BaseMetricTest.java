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

package com.codeheadsystems.metrics.test;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.Slf4jReporter;
import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.helper.DropwizardMetricsHelper;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseMetricTest {

    protected static MeterRegistry meterRegistry;
    protected static Reporter reporter;
    protected Metrics metrics;

    @BeforeAll
    protected static void setupDropWizard() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        reporter = Slf4jReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        meterRegistry = new DropwizardMetricsHelper().instrument(metricRegistry);
    }

    @AfterAll
    protected static void report() throws IOException {
        reporter.close();
    }

    @BeforeEach
    protected void setup() {
        metrics = new Metrics(meterRegistry);
    }

}
