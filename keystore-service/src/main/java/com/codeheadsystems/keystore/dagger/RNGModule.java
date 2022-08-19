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

import com.codeheadsystems.keystore.common.model.Rng;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the random provider we want to use.
 */
@Module(includes = {RNGModule.Binder.class})
public class RNGModule {

  public static final String PROVIDED_RNG = "RNG";
  private static final Logger LOGGER = LoggerFactory.getLogger(RNGModule.class);

  @Provides
  @Singleton
  @Named(PROVIDED_RNG)
  public Rng rng(@Named(Binder.RNG_IMPL) final Optional<Rng> suppliedRng) {
    final Rng rng = suppliedRng.orElseGet(this::defaultRNG);
    LOGGER.info("RNG {}", rng.getClass().getName());
    return rng;
  }

  public Rng defaultRNG() {
    try {
      final SecureRandom random = SecureRandom.getInstance("NativePRNG");
      return random::nextBytes;
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("No native PRNG found", e);
    }
  }

  @Module
  public interface Binder {
    String RNG_IMPL = "RNG_IMPL";

    @BindsOptionalOf
    @Named(RNG_IMPL)
    Rng rng();
  }

}
