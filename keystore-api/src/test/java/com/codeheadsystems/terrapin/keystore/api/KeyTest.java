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

package com.codeheadsystems.terrapin.keystore.api;

import com.codeheadsystems.test.model.BaseJacksonTest;

class KeyTest extends BaseJacksonTest<Key> {

  @Override
  protected Class<Key> getBaseClass() {
    return Key.class;
  }

  @Override
  protected Key getInstance() {
    return ImmutableKey.builder()
        .owner("owner")
        .id("id")
        .version(2L)
        .status("status")
        .key(new byte[]{5, 6, 7})
        .build();
  }
}