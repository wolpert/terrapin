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

package com.codeheadsystems.keystore.manager;

import static com.codeheadsystems.keystore.dagger.RngModule.PROVIDED_RNG;

import com.codeheadsystems.keystore.common.helper.DataHelper;
import com.codeheadsystems.keystore.common.model.Rng;
import com.codeheadsystems.keystore.exception.AlreadyExistsException;
import com.codeheadsystems.keystore.server.dao.KeyDao;
import com.codeheadsystems.keystore.server.dao.model.ImmutableKey;
import com.codeheadsystems.keystore.server.dao.model.ImmutableKeyVersionIdentifier;
import com.codeheadsystems.keystore.server.dao.model.Key;
import com.codeheadsystems.keystore.server.dao.model.KeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.KeyVersionIdentifier;
import java.util.Date;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic manager to update or create keys. Business logic for the resource.
 */
@Singleton
public class KeyStoreAdminManager {

  /**
   * The constant KEY_SIZE.
   */
  public static final int KEY_SIZE = 32;
  /**
   * The constant AUX_SIZE.
   */
  public static final int AUX_SIZE = 16;
  private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreAdminManager.class);
  private final KeyDao keyDao;
  private final Rng rng;
  private final DataHelper dataHelper;

  /**
   * Default constructor.
   *
   * @param keyDao     we use to store the keys.
   * @param rng        the P/RNG for key generation.
   * @param dataHelper helper class to update strings.
   */
  @Inject
  public KeyStoreAdminManager(final KeyDao keyDao,
                              @Named(PROVIDED_RNG) final Rng rng,
                              final DataHelper dataHelper) {
    LOGGER.info("KeyManager({},{},{})", keyDao, rng, dataHelper);
    this.keyDao = keyDao;
    this.rng = rng;
    this.dataHelper = dataHelper;
  }

  /**
   * Creates a new key. Note, do not call this to rotate keys. You don't really create versions directly,
   * rather you rotate keys to create new veresions.
   *
   * @param identifier identifier for creation.
   * @return a ney key.
   * @throws AlreadyExistsException if the key already exists.
   */
  public Key create(final KeyIdentifier identifier) throws AlreadyExistsException {
    LOGGER.debug("create({})", identifier);
    final Optional<Key> currentKey = keyDao.load(identifier);
    if (currentKey.isPresent()) {
      throw new AlreadyExistsException();
    }
    final KeyVersionIdentifier newKeyIdentifier = ImmutableKeyVersionIdentifier.builder()
        .owner(identifier.owner()).key(identifier.key()).version(1L).build();
    final byte[] secret = new byte[KEY_SIZE];
    rng.random(secret);
    final byte[] aux = new byte[AUX_SIZE];
    rng.random(aux);
    final Key key = ImmutableKey.builder()
        .keyVersionIdentifier(newKeyIdentifier)
        .type("256")
        .active(true)
        .createDate(new Date())
        .value(secret)
        .aux(aux)
        .build();
    keyDao.store(key);
    dataHelper.clear(secret); // secret is copied to make the key.
    return key;
  }
}
