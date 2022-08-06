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

package com.codeheadsystems.oop.mock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HasherTest {

  private static final String SYSTEM = "DEFAULT";

  @Test
  void hash() {
    final Hasher hasher = new Hasher(SYSTEM);
    final String result = hasher.hash("a", "b", "c");

    assertThat(result)
        .isEqualTo("a.b.c");
  }

  @Test
  void namespace_empty() {
    final Hasher hasher = new Hasher(SYSTEM);
    final String result = hasher.namespace(HasherTest.class);

    assertThat(result)
        .isEqualTo("DEFAULT:com.codeheadsystems.oop.mock.HasherTest");
  }

  @Test
  void namespace_notEmpty() {
    final String app = "OppMock";
    final Hasher hasher = new Hasher(app);
    final String result = hasher.namespace(HasherTest.class);

    assertThat(result)
        .isEqualTo(app + ":com.codeheadsystems.oop.mock.HasherTest");
  }
}