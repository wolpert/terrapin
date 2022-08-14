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

/**
 * Standard interface to encrypt/decrypt.
 */
public interface Cryptor {

  byte[] encrypt(final byte[] key, final byte[] clear, int ivLength) throws CryptoException;

  byte[] decrypt(final byte[] key, final byte[] payload, int ivLength) throws CryptoException;

}
