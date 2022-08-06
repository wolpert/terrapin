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

import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide your own meter registry named "Provided Meter Registry" else you get the default.
 */
@Module(includes = MetricsModule.Binder.class)
public class MetricsModule {
  public static final String PROVIDED_METER_REGISTRY = "Provided Meter Registry";
  public static final String METER_REGISTRY = "Meter Registry";
  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsModule.class);

  private final MeterRegistry override;

  public MetricsModule() {
    this(null);
  }

  public MetricsModule(final MeterRegistry override) {
    this.override = override;
  }

  @Provides
  @Singleton
  @Named(METER_REGISTRY)
  public MeterRegistry meterRegistry(@Named(PROVIDED_METER_REGISTRY) Optional<MeterRegistry> optionalMeterRegistry) {
    LOGGER.info("Provided metric: {}", optionalMeterRegistry.isPresent());
    if (override != null) {
      LOGGER.info("Override: {}", override);
      return override;
    } else {
      return optionalMeterRegistry.orElseGet(SimpleMeterRegistry::new);
    }
  }

  @Module
  public interface Binder {

    @Named(PROVIDED_METER_REGISTRY)
    @BindsOptionalOf
    MeterRegistry meterRegistry();

  }

}
