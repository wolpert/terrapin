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

import dagger.Module;
import dagger.Provides;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * To use this, you need to define provider for a MetricsImplementationFactory. Else you will get the null one.
 */
@Module
public class MetricsFactoryModule {

    public static final String DEFAULT_SUCCESS = ".success";
    public static final String DEFAULT_FAIL = ".fail";
    public static final String METRICS_IMPLEMENTATION = "METRICS_IMPLEMENTATION_FACTORY";
    public static final String METRIC_SUCCESS_NAME = "METRIC_SUCCESS_NAME";
    public static final String METRIC_FAIL_NAME = "METRIC_FAIL_NAME";

    @Provides
    @Singleton
    public MetricsFactory metricsFactory(@Named(METRICS_IMPLEMENTATION) final Optional<MetricsImplementation> metricsImplementation,
                                         @Named(METRIC_SUCCESS_NAME) final Optional<String> successName,
                                         @Named(METRIC_FAIL_NAME) final Optional<String> failName) {
        return new MetricsFactory(metricsImplementation.orElseGet(NullMetricsImplementation::new),
                successName.orElse(DEFAULT_SUCCESS),
                failName.orElse(DEFAULT_FAIL));
    }

}
