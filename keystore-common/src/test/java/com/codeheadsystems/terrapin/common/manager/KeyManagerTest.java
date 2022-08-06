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

import com.codeheadsystems.terrapin.common.crypt.CryptorType;
import com.codeheadsystems.terrapin.common.model.RNG;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KeyManagerTest {

    private Random random;
    private RNG rng;
    private KeyManager keyManager;

    public static Stream<Arguments> cryptoTypes() {
        return Arrays.stream(CryptorType.values())
                .map(Arguments::of);
    }

    @BeforeEach
    public void setup() {
        random = new Random();
        rng = random::nextBytes;
        keyManager = new KeyManager(rng);
    }

    @ParameterizedTest
    @MethodSource("cryptoTypes")
    public void testType(final CryptorType type) {
        final byte[] result = keyManager.generate(type);
        assertThat(result)
                .isNotEmpty()
                .hasSize(type.getIvLength() + type.getKeyLength());
    }

}