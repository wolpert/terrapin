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

package com.codeheadsystems.terrapin.common.crypt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.terrapin.common.exception.CryptoException;
import java.util.Random;
import org.bouncycastle.crypto.modes.GCMSIVBlockCipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AeadCipherCryptorTest {

  private Random random;
  private AeadCipherCryptor<GCMSIVBlockCipher> cryptor;

  @BeforeEach
  void setup() {
    random = new Random();
    cryptor = new AeadCipherCryptor<>(GCMSIVBlockCipher::new);
  }

  @Test
  public void roundTrip() throws CryptoException {
    final byte[] key = getKey(32, 12);
    final byte[] clearPayload = new byte[256];
    random.nextBytes(clearPayload);

    final byte[] encryptedPayload = cryptor.encrypt(key, clearPayload, 12);
    assertThat(encryptedPayload)
        .isNotEmpty()
        .isNotEqualTo(clearPayload);

    final byte[] decryptedPayload = cryptor.decrypt(key, encryptedPayload, 12);
    assertThat(decryptedPayload)
        .isNotEmpty()
        .isEqualTo(clearPayload);
  }

  @Test
  public void badIVLength() {
    final byte[] key = getKey(32, 16);
    final byte[] clearPayload = new byte[256];
    random.nextBytes(clearPayload);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> cryptor.encrypt(key, clearPayload, 16));
  }

  @Test
  public void noIV() {
    final byte[] key = getKey(32, 0);
    final byte[] clearPayload = new byte[256];
    random.nextBytes(clearPayload);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> cryptor.encrypt(key, clearPayload, 0));
  }

  @Test
  public void badDecrypt() {
    final byte[] key = getKey(32, 12);
    final byte[] clearPayload = new byte[256];
    random.nextBytes(clearPayload);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> cryptor.decrypt(key, clearPayload, 16));
  }

  public byte[] getKey(final int keyLen, final int ivLen) {
    final byte[] result = new byte[keyLen + ivLen];
    random.nextBytes(result);
    return result;
  }
}