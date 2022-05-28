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

package com.codeheadsystems.oop.mock.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceLookupManagerTest {

    private ResourceLookupManager manager;

    @BeforeEach
    void setup() {
        manager = new ResourceLookupManager(Optional.empty());
    }

    @Test
    void inputStream() {
        assertThat(manager.inputStream("logback.xml"))
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    void inputStream_notFound() {
        assertThat(manager.inputStream("I do not ExIsT"))
                .isNotNull()
                .isEmpty();
    }
}