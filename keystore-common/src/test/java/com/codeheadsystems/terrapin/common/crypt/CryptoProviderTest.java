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

import com.codeheadsystems.terrapin.common.manager.ConstructorManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CryptoProviderTest {

    private CryptoProvider cryptoProvider;

    @BeforeEach
    void setup() {
        cryptoProvider = new CryptoProvider(new ConstructorManager());
    }

    @Test
    public void testSameCrypto() {
        final Cryptor first = cryptoProvider.cryptor(CryptorType.AES_256_GCM_SIV);
        final Cryptor second = cryptoProvider.cryptor(CryptorType.AES_256_GCM_SIV);

        assertThat(first)
                .isNotNull()
                .isSameAs(second);
    }

}