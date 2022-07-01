// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codeheadsystems.metrics.MetricsFactoryModule;
import com.codeheadsystems.metrics.MetricsImplementation;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * We include Metrics Factory. Use that for these metrics.
 */
@Module(includes = MetricsFactoryModule.class)
public class DropwizardMetricsModule {

    @Provides
    @Singleton
    MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Provides
    @Singleton
    MetricsImplementation metricsImplementation(final DropwizardMetricsImplementation implementation) {
        return implementation;
    }

}
