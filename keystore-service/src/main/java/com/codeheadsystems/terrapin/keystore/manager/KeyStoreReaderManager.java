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

package com.codeheadsystems.terrapin.keystore.manager;

import com.codeheadsystems.terrapin.server.dao.KeyDao;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the reads only.
 */
@Singleton
public class KeyStoreReaderManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreReaderManager.class);
  private final KeyDao keyDAO;

  @Inject
  public KeyStoreReaderManager(final KeyDao keyDAO) {
    LOGGER.info("KeyStoreReaderManager({})", keyDAO);
    this.keyDAO = keyDAO;
  }

  public Optional<Key> getKey(final KeyVersionIdentifier identifier) {
    LOGGER.debug("getKey({})", identifier);
    return keyDAO.load(identifier);
  }

  public Optional<Key> getKey(final KeyIdentifier identifier) {
    LOGGER.debug("getKey({})", identifier);
    return keyDAO.load(identifier);
  }
}
