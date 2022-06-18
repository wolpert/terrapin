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

package com.codeheadsystems.oop.mock.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObjectMapperFactoryTest {

    private ObjectMapperFactory objectMapperFactory;

    @BeforeEach
    public void setup() {
        objectMapperFactory = new ObjectMapperFactory();
    }

    @Test
    void objectMapper() {
        assertThat(objectMapperFactory.objectMapper())
                .isNotNull()
                .extracting("RegisteredModuleIds")
                .asInstanceOf(InstanceOfAssertFactories.COLLECTION)
                .isNotNull()
                .isNotEmpty()
                .contains(Jdk8Module.class.getCanonicalName());
    }
}