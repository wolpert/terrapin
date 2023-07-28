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

package com.codeheadsystems.keystore.config;

import io.dropwizard.Configuration;

/**
 * The configuration for drop wizard.
 */
public class KeyStoreConfiguration extends Configuration {

  private String dropWizardFactory;
  private DataStore dataStore;

  /**
   * Gets data store.
   *
   * @return the data store
   */
  public DataStore getDataStore() {
    return dataStore;
  }

  /**
   * Sets data store.
   *
   * @param dataStore the data store
   */
  public void setDataStore(final DataStore dataStore) {
    this.dataStore = dataStore;
  }

  /**
   * Gets drop wizard factory.
   *
   * @return the drop wizard factory
   */
  public String getDropWizardFactory() {
    return dropWizardFactory;
  }

  /**
   * Sets drop wizard factory.
   *
   * @param dropWizardFactory the drop wizard factory
   */
  public void setDropWizardFactory(final String dropWizardFactory) {
    this.dropWizardFactory = dropWizardFactory;
  }
}
