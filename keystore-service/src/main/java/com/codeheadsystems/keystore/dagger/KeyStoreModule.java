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

package com.codeheadsystems.keystore.dagger;

import com.codeheadsystems.keystore.config.KeyStoreConfiguration;
import com.codeheadsystems.metrics.dagger.MetricsModule;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * A super-set of modules used for the keystore service.
 */
@Module(includes = {
    HealthCheckModule.class,
    MetricsModule.class,
    ResourceModule.class,
    RngModule.class
})
public class KeyStoreModule {

  private final KeyStoreConfiguration keyStoreConfiguration;

  /**
   * The default constructor.
   *
   * @param keyStoreConfiguration the configuration from dropwizard.
   */
  public KeyStoreModule(final KeyStoreConfiguration keyStoreConfiguration) {
    this.keyStoreConfiguration = keyStoreConfiguration;
  }

  /**
   * Key store configuration key store configuration.
   *
   * @return the key store configuration
   */
  @Provides
  @Singleton
  public KeyStoreConfiguration keyStoreConfiguration() {
    return keyStoreConfiguration;
  }

}
