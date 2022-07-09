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

import com.codeheadsystems.metrics.MetricsHelper;
import com.codeheadsystems.metrics.helper.PoolableThreadLocalMetricsHelper;
import com.codeheadsystems.metrics.helper.ThreadLocalMetricsHelper;
import dagger.Binds;
import dagger.Module;
import javax.inject.Singleton;

/**
 * This interface contains two modules. You have to pick one to get the metrics helper you want.
 */
public interface MetricHelperModule {

    @Module
    public interface Default {
        @Binds
        @Singleton
        MetricsHelper metricsHelper(ThreadLocalMetricsHelper helper);
    }

    @Module
    public interface Pooled {
        @Binds
        @Singleton
        MetricsHelper metricsHelper(PoolableThreadLocalMetricsHelper helper);
    }

}
