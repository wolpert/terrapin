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

package com.codeheadsystems.terrapin.common.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.terrapin.common.crypt.AEADCipherCryptor;
import com.codeheadsystems.terrapin.common.crypt.CryptorType;
import com.codeheadsystems.terrapin.common.model.RNG;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;
import org.bouncycastle.crypto.modes.AEADCipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EncryptionManagerTest {

  private Random random = new Random();
  private RNG rng = random::nextBytes;
  private KeyManager keyManager = new KeyManager(rng);
  private LoadingCache<CryptorType, AEADCipherCryptor<? extends AEADCipher>> cache =
      CacheBuilder.newBuilder().build(CacheLoader.from(type -> new AEADCipherCryptor<>(type.getSupplier())));

  private EncryptionManager encryptionManager;

  public static Stream<Arguments> cryptoTypes() {
    return Arrays.stream(CryptorType.values())
        .map(Arguments::of);
  }

  @BeforeEach
  public void setup() {
    encryptionManager = new EncryptionManager(keyManager, cache);
  }

  @ParameterizedTest
  @MethodSource("cryptoTypes")
  public void testRoundTrip(final CryptorType type) {
    final byte[] key = encryptionManager.keyFor(type);
    final byte[] payload = payload();
    final byte[] encryptedPayload = encryptionManager.encrypt(type, key, payload);
    final byte[] decryptedPayload = encryptionManager.decrypt(type, key, encryptedPayload);

    assertThat(payload)
        .isNotEmpty()
        .isEqualTo(decryptedPayload);
    assertThat(encryptedPayload)
        .isNotEqualTo(payload);
    // fails
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> encryptionManager.decrypt(type, key, payload));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> encryptionManager.decrypt(type, payload, payload));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> encryptionManager.encrypt(type, payload, payload));
  }

  @Test
  public void testCacheFailure() {
    final byte[] key = payload();
    final byte[] payload = payload();
    encryptionManager = new EncryptionManager(keyManager, CacheBuilder.newBuilder().build(CacheLoader
        .from(type -> {
          throw new IllegalArgumentException();
        })));
    assertThatExceptionOfType(UncheckedExecutionException.class)
        .isThrownBy(() -> encryptionManager.decrypt(CryptorType.AES_128_GCM, key, payload));
    assertThatExceptionOfType(UncheckedExecutionException.class)
        .isThrownBy(() -> encryptionManager.encrypt(CryptorType.AES_128_GCM, key, payload));
  }

  private byte[] payload() {
    final byte[] payload = new byte[256];
    rng.random(payload);
    return payload;
  }

}