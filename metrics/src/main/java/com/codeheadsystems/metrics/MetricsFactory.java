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

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsFactory.class);

    private final MetricsImplementation metricsImplementation;
    private final String successName;
    private final String failName;

    public MetricsFactory(final MetricsImplementation metricsImplementation,
                          final String successName,
                          final String failName) {
        this.metricsImplementation = metricsImplementation;
        this.successName = successName;
        this.failName = failName;
        LOGGER.info("MetricsFactory({},{},{})", this.metricsImplementation, this.successName, this.failName);
    }

    public Metrics build() {
        return new Metrics(metricsImplementation, successName, failName);
    }

    public void with(final Consumer<Metrics> consumer) {
        with(m -> {
            consumer.accept(m);
            return null;
        });
    }

    public <R> R with(final Function<Metrics,R> function) {
        try(Metrics metrics = build()) {
            return function.apply(metrics);
        } catch (IOException e) {
            LOGGER.error("Metrics Fail", e);
            throw new IllegalStateException("Metrics fail", e);
        }
    }
}
