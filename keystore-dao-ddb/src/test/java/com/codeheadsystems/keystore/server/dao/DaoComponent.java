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

package com.codeheadsystems.keystore.server.dao;

import static com.codeheadsystems.metrics.dagger.MetricsModule.PROVIDED_METER_REGISTRY;

import com.codeheadsystems.keystore.server.dao.ddb.dagger.DdbModule;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.micrometer.core.instrument.MeterRegistry;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Sample Dao component needed
 */
@Singleton
@Component(modules = {DdbModule.class, DaoComponent.OurMeterModule.class})
public interface DaoComponent {

  KeyDao keyDao();

  @Module
  class OurMeterModule {

    private final MeterRegistry meterRegistry;

    public OurMeterModule(final MeterRegistry meterRegistry) {
      this.meterRegistry = meterRegistry;
    }

    @Provides
    @Singleton
    @Named(PROVIDED_METER_REGISTRY)
    public MeterRegistry meterRegistry() {
      return meterRegistry;
    }
  }

}
