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

package com.codeheadsystems.metrics.dagger;

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.impl.MetricsFactory;
import com.codeheadsystems.metrics.vendor.MetricsVendor;
import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import java.time.Clock;
import java.util.function.Supplier;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * To use this, you need to define provider for a Metrics Vendor. Else you will get the null one.
 * Check the logs when you get the metrics factory from the dagger container.
 */
@Module(includes = MetricsFactoryModule.Utilities.class)
public interface MetricsFactoryModule {
    /**
     * This will be set when your dagger modules includes the vendor implementation. Metrics uses can ignore this.
     */
    @BindsOptionalOf
    MetricsVendor metricsVendor();

    /**
     * Metrics users can directly use the metrics factory as a supplier by using this directly. They will be the same
     * thing. Just inject the @Named supplier into your class, and you are done. However, you should use a metrics
     * helper to reduce the code bloat. But your call.
     */
    @Binds
    @Singleton
    @Named("METRICS_SUPPLIER")
    Supplier<Metrics> metricsSupplier(MetricsFactory metricsFactory);

    @Module
    class Utilities {

        @Provides
        @Singleton
        public Clock clock() {
            return Clock.systemUTC();
        }

    }

}
