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

import com.codahale.metrics.MetricRegistry;
import com.codeheadsystems.metrics.MetricsImplementation;
import java.io.IOException;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Currently dimensions are ignored for dropwizard.
 */
@Singleton
public class DropwizardMetricsImplementation implements MetricsImplementation {

    private final MetricRegistry metricRegistry;

    @Inject
    public DropwizardMetricsImplementation(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void count(final String name, final long value) {
        metricRegistry.histogram(name).update(value);
    }

    @Override
    public <R> R time(final String name, final Supplier<R> supplier) {
        return metricRegistry.timer(name).timeSupplier(supplier);
    }

    @Override
    public void close() throws IOException {

    }
}
