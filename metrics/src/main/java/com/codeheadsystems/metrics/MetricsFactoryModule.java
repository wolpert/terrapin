// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

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
    public static final String METRICS_IMPLEMENTATION_FACTORY = "METRICS_IMPLEMENTATION_FACTORY";
    public static final String METRIC_SUCCESS_NAME = "METRIC_SUCCESS_NAME";
    public static final String METRIC_FAIL_NAME = "METRIC_FAIL_NAME";

    @Provides
    @Singleton
    public MetricsFactory metricsFactory(@Named(METRICS_IMPLEMENTATION_FACTORY) final Optional<MetricsImplementationFactory> metricsImplementationFactory,
                                         @Named(METRIC_SUCCESS_NAME) final Optional<String> successName,
                                         @Named(METRIC_FAIL_NAME) final Optional<String> failName) {
        return new MetricsFactory(metricsImplementationFactory.orElseGet(NullMetricsImplementationFactory::new),
                successName.orElse(DEFAULT_SUCCESS),
                failName.orElse(DEFAULT_FAIL));
    }

}
