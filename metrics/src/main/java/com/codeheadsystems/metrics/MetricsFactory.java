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

import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initially, the metrics factory is a simplistic supplier. As we get more complicated, we can extend this as needed.
 * But for now, just follow the supplier pattern.
 */
@Singleton
public class MetricsFactory implements Supplier<Metrics>{
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsFactory.class);
    private static final Metrics NULL_METRICS = new NullMetrics();
    private final Supplier<Metrics> metricsSupplier;

    public MetricsFactory(Optional<MetricsVendor> metricsVendor){
        LOGGER.info("MetricsFactory({}})", metricsVendor);
        metricsSupplier = metricsVendor               // This is a little funky looking but...
                .map(this::metricsImplementation)     // returns the supplier as built by the method
                .orElse(MetricsFactory::nullMetrics); // returns the method which IS the supplier. Yeah, I know...
    }

    public static Metrics nullMetrics() {
        return NULL_METRICS;
    }

    /**
     * Helper method to simplify the constructor. Else the map is too confusing.
     */
    private Supplier<Metrics> metricsImplementation(final MetricsVendor v) {
        return () -> new MetricsImplementation(v);
    }

    @Override
    public Metrics get() {
        return metricsSupplier.get();
    }
}