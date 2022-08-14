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

import com.codeheadsystems.keystore.common.crypt.Cryptor;
import com.codeheadsystems.keystore.common.crypt.CryptorType;
import com.codeheadsystems.keystore.common.exception.CryptoException;
import com.google.common.cache.LoadingCache;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is basically a facade over what it takes to get bouncy castle to work.
 * However, this could be replaced with standard JSSE plugin as needed. The reason
 * bouncy castle is picked was it gets updated faster, supports more algo than JSSE
 * if that's important, and keysize is not limited by Sun's outdated mechanism.
 */
@Singleton
public class EncryptionManager {

  public static final String LOADING_CACHE = "LoadingCache";
  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionManager.class);
  private final KeyManager keyManager;

  private final LoadingCache<CryptorType, Cryptor> cache;

  /**
   * Default constructor.
   *
   * @param keyManager key manager for creating keys.
   * @param cache      the cache for the ciphers.
   */
  @Inject
  public EncryptionManager(final KeyManager keyManager,
                           @Named(LOADING_CACHE) final LoadingCache<CryptorType, Cryptor> cache) {
    LOGGER.info("EncryptionManager({})", keyManager);
    this.keyManager = keyManager;
    this.cache = cache;
  }

  public byte[] keyFor(final CryptorType type) {
    LOGGER.debug("keyFor({})", type);
    return keyManager.generate(type);
  }

  /**
   * Encrypts for the given cipher.
   *
   * @param type    of cipher to use.
   * @param key     to encrypt the data.
   * @param payload the payload to encrypt.
   * @return encrypted payload.
   */
  public byte[] encrypt(final CryptorType type, final byte[] key, final byte[] payload) {
    LOGGER.debug("encrypt({})", type);
    try {
      return cache.getUnchecked(type)
          .encrypt(key, payload, type.getIvLength());
    } catch (CryptoException e) {
      throw new IllegalArgumentException(e);
    }
  }


  /**
   * Decrypts for the given cipher.
   *
   * @param type    of cipher to use.
   * @param key     to decrypt the data.
   * @param payload the payload to decrypt.
   * @return decrypted payload..
   */
  public byte[] decrypt(final CryptorType type, final byte[] key, final byte[] payload) {
    LOGGER.debug("decrypt({})", type);
    try {
      return cache.getUnchecked(type)
          .decrypt(key, payload, type.getIvLength());
    } catch (CryptoException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
