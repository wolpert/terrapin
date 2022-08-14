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

package com.codeheadsystems.keystore.common.crypt;

import com.codeheadsystems.keystore.common.exception.CryptoException;
import java.util.function.Supplier;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.AEADCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a common way to use bouncy castle ciphers.
 *
 * @param <T> type of BC cipher we manage in a thread-safe way.
 */
public class AeadCipherCryptor<T extends AEADCipher> implements Cryptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AeadCipherCryptor.class);

  private final ThreadLocal<T> blockCipherThreadLocal;
  private final String algorithm;

  /**
   * Default constructor.
   *
   * @param cipherSupplier the supplier to create the cipher.
   */
  public AeadCipherCryptor(final Supplier<T> cipherSupplier) {
    this.blockCipherThreadLocal = ThreadLocal.withInitial(cipherSupplier);
    this.algorithm = blockCipherThreadLocal.get().getAlgorithmName();
    LOGGER.info("AEADCipherCryptor({})", algorithm);
  }

  @Override
  public byte[] encrypt(final byte[] key, final byte[] payload, int ivLength) throws CryptoException {
    LOGGER.debug("{}: encrypt", algorithm);
    final T cypher = setupCrypto(key, true, ivLength);
    return executeCrypto(payload, cypher);
  }

  @Override
  public byte[] decrypt(final byte[] key, final byte[] payload, int ivLength) throws CryptoException {
    LOGGER.debug("{}: decrypt", algorithm);
    final T cypher = setupCrypto(key, false, ivLength);
    return executeCrypto(payload, cypher);
  }

  private T setupCrypto(final byte[] key, final boolean encrypt, int ivLength) {
    final CipherParameters parameters = getParameters(key, ivLength);
    var cypher = blockCipherThreadLocal.get();
    cypher.init(encrypt, parameters);
    return cypher;
  }

  private byte[] executeCrypto(final byte[] payload, final T cypher) throws CryptoException {
    final int outputSize = cypher.getOutputSize(payload.length);
    final byte[] result = new byte[outputSize];
    int processed = cypher.processBytes(payload, 0, payload.length, result, 0);
    try {
      processed += cypher.doFinal(result, processed);
      LOGGER.trace("Avail:{} Processed:{} match:{}", outputSize, processed, outputSize == processed);
    } catch (InvalidCipherTextException e) {
      throw new CryptoException(e);
    } finally {
      cypher.reset();
    }
    return result;
  }

  private CipherParameters getParameters(final byte[] key, int ivLength) {
    final int keyLength = key.length - ivLength;
    final KeyParameter keyParams = new KeyParameter(key, 0, keyLength);
    if (ivLength > 0) {
      return new ParametersWithIV(keyParams, key, keyLength, ivLength);
    } else {
      return keyParams;
    }
  }
}
