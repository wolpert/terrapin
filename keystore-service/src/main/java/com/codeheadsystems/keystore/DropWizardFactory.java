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

package com.codeheadsystems.keystore;

import com.codeheadsystems.keystore.config.KeyStoreConfiguration;
import com.codeheadsystems.keystore.dagger.DropWizardComponent;
import io.micrometer.core.instrument.MeterRegistry;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Must implement this method.
 */
public interface DropWizardFactory {

  /**
   * Get the current dropwizard factory from the keystore configuration. Lots of ways this can fail.
   *
   * @param configuration with the factory class.
   * @return an instance.
   */
  static DropWizardFactory getDropWizardFactory(final KeyStoreConfiguration configuration) {
    try {
      final Class<DropWizardFactory> clazz =
          (Class<DropWizardFactory>) Class.forName(configuration.getDropWizardFactory());
      final Constructor<DropWizardFactory> constructor = clazz.getConstructor();
      return constructor.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  DropWizardComponent build(final KeyStoreConfiguration configuration,
                            final MeterRegistry meterRegistry);

}
