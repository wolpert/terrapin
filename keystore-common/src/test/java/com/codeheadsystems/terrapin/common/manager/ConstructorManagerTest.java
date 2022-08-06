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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConstructorManagerTest {

    private ConstructorManager constructorManager;

    @BeforeEach
    public void setup() {
        constructorManager = new ConstructorManager();
    }

    @Test
    public void happyPath() {
        assertThat(constructorManager.defaultConstructor(ConstructorManager.class))
                .isNotNull();
    }

    @Test
    public void unhappyPath() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> constructorManager.defaultConstructor(AEADCipherCryptor.class));
    }

}