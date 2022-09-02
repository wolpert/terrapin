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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.keystore.config.DataStore;
import com.codeheadsystems.keystore.config.KeyStoreConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DropWizardFactoryTest {
  @Mock private DataStore dataStore;
  private KeyStoreConfiguration configuration;

  @BeforeEach
  public void setup() {
    configuration = new KeyStoreConfiguration();
    configuration.setDataStore(dataStore);
  }

  @Test
  public void getDropWizardFactory_badClass() {
    configuration.setDropWizardFactory(Object.class.getName());
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> DropWizardFactory.getDropWizardFactory(configuration));
  }

  @Test
  public void getDropWizardFactory_goodClass() {
    configuration.setDropWizardFactory(TestDropWizardFactoryImpl.class.getName());

    assertThat(DropWizardFactory.getDropWizardFactory(configuration))
        .isNotNull();
  }

}