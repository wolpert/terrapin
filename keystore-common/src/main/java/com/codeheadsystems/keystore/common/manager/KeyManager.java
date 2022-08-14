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

package com.codeheadsystems.keystore.common.manager;

import com.codeheadsystems.keystore.common.crypt.CryptorType;
import com.codeheadsystems.keystore.common.model.Rng;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages keys generation. Can be used server side or client.
 */
@Singleton
public class KeyManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyManager.class);

  private final Rng rng;

  /**
   * Default constructor.
   *
   * @param rng for creating keys.
   */
  @Inject
  public KeyManager(final Rng rng) {
    LOGGER.info("KeyManager({})", rng);
    this.rng = rng;
  }

  /**
   * Generates a key for this type.
   *
   * @param type of cipher we want to use.
   * @return bytes which is a newly generated key.
   */
  public byte[] generate(final CryptorType type) {
    LOGGER.debug("generate({})", type);
    final byte[] result = new byte[type.getKeyLength() + type.getIvLength()];
    rng.random(result);
    return result;
  }
}
