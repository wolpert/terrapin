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

package com.codeheadsystems.keystore.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.keystore.server.dao.model.ImmutableKey;
import com.codeheadsystems.keystore.server.dao.model.ImmutableKeyVersionIdentifier;
import com.codeheadsystems.keystore.server.dao.model.Key;
import com.codeheadsystems.keystore.server.dao.model.KeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.KeyVersionIdentifier;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApiConverterTest {

  public static final String OWNER = "fred";
  public static final String KEY_ID = "somekey";
  public static final long VERSION = 10L;
  public static final byte[] BYTES = new byte[]{4, 5, 6};
  private static final Key API_KEY = ImmutableKey.builder()
      .keyVersionIdentifier(ImmutableKeyVersionIdentifier.builder()
          .owner(OWNER).key(KEY_ID).version(VERSION).build())
      .active(true)
      .createDate(new Date())
      .type("AES")
      .value(BYTES)
      .aux(BYTES)
      .build();
  private ApiConverter converter;

  @BeforeEach
  public void setup() {
    converter = new ApiConverter();
  }

  @Test
  void toDaoKeyVersionIdentifier() {
    final KeyVersionIdentifier result = converter.toDaoKeyVersionIdentifier(OWNER, KEY_ID, VERSION);
    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("owner", OWNER)
        .hasFieldOrPropertyWithValue("key", KEY_ID)
        .hasFieldOrPropertyWithValue("version", VERSION);
  }

  @Test
  void toDaoKeyIdentifier() {
    final KeyIdentifier result = converter.toDaoKeyIdentifier(OWNER, KEY_ID);
    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("owner", OWNER)
        .hasFieldOrPropertyWithValue("key", KEY_ID);
  }

  @Test
  void toApiKey() {
    final com.codeheadsystems.keystore.api.Key result = converter.toApiKey(API_KEY);
    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("owner", OWNER)
        .hasFieldOrPropertyWithValue("id", KEY_ID)
        .hasFieldOrPropertyWithValue("version", VERSION)
        .hasFieldOrPropertyWithValue("status", ApiConverter.ACTIVE)
        .hasFieldOrPropertyWithValue("key", BYTES);
  }

  @Test
  void toApiKey_inactive() {
    final Key inactiveKey = ImmutableKey.copyOf(API_KEY)
        .withActive(false);

    final com.codeheadsystems.keystore.api.Key result = converter.toApiKey(inactiveKey);
    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("owner", OWNER)
        .hasFieldOrPropertyWithValue("id", KEY_ID)
        .hasFieldOrPropertyWithValue("version", VERSION)
        .hasFieldOrPropertyWithValue("status", ApiConverter.INACTIVE)
        .hasFieldOrPropertyWithValue("key", BYTES);
  }
}