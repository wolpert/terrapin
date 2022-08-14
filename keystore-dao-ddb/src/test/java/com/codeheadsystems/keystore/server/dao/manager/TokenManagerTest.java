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

package com.codeheadsystems.keystore.server.dao.manager;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.keystore.common.factory.ObjectMapperFactory;
import com.codeheadsystems.keystore.common.helper.DataHelper;
import com.codeheadsystems.keystore.common.manager.JsonManager;
import com.codeheadsystems.keystore.server.dao.ddb.factory.DdbObjectMapperFactory;
import com.codeheadsystems.keystore.server.dao.ddb.manager.TokenManager;
import com.codeheadsystems.keystore.server.dao.model.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class TokenManagerTest {

  private static final String KEY = "KEY";
  private static final String OWNER = "OWNER";
  private static final AttributeValue VALUE = AttributeValue.builder().s(OWNER).build();
  private static final Map<String, AttributeValue> MAP = Map.of(KEY, VALUE);

  private TokenManager tokenManager;

  /**
   * We set this us with real internals to ensure we are getting the serialization right.
   */
  @BeforeEach
  void setup() {
    final ObjectMapper objectMapper = new DdbObjectMapperFactory(new ObjectMapperFactory()).generate();
    final JsonManager jsonManager = new JsonManager(objectMapper);
    final DataHelper dataHelper = new DataHelper();
    tokenManager = new TokenManager(dataHelper, jsonManager);
  }

  @Test
  public void roundTrip() {
    final Token token = tokenManager.serialize(MAP);
    final Map<String, AttributeValue> result = tokenManager.deserialize(token);
    assertThat(result)
        .isEqualTo(MAP);
  }

}