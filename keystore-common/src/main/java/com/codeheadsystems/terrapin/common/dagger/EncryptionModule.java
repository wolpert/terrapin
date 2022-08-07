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

package com.codeheadsystems.terrapin.common.dagger;

import static com.codeheadsystems.terrapin.common.manager.EncryptionManager.LOADING_CACHE;

import com.codeheadsystems.terrapin.common.crypt.AeadCipherCryptor;
import com.codeheadsystems.terrapin.common.crypt.Cryptor;
import com.codeheadsystems.terrapin.common.crypt.CryptorType;
import com.codeheadsystems.terrapin.common.model.Rng;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dagger.Module;
import dagger.Provides;
import java.security.SecureRandom;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Provides components needed for encryption.
 */
@Module
public class EncryptionModule {

  /**
   * Provides a loading cache of cryptors we can use.
   *
   * @return the loading cache.
   */
  @Provides
  @Singleton
  @Named(LOADING_CACHE)
  public LoadingCache<CryptorType, Cryptor> cache() {
    // Note, we really want a thread with each supplier, not type. But this works anyways.
    // The memory hit isn't high.
    return CacheBuilder.newBuilder().build(CacheLoader.from(type -> new AeadCipherCryptor<>(type.getSupplier())));
  }

  /**
   * Gives us access to the RNG that is secure here.
   *
   * @return the rng.
   */
  @Provides
  @Singleton
  public Rng rng() {
    final SecureRandom random = new SecureRandom();
    return random::nextBytes;
  }

}
