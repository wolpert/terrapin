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

package com.codeheadsystems.keystore.server.dao.ddb.manager;

import com.codeheadsystems.keystore.common.helper.DataHelper;
import com.codeheadsystems.keystore.common.manager.JsonManager;
import com.codeheadsystems.keystore.server.dao.model.ImmutableToken;
import com.codeheadsystems.keystore.server.dao.model.Token;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Singleton
public class TokenManager {

  public static final TypeReference<HashMap<String, AttributeValue.Builder>> TYPE_REFERENCE = new TypeReference<>() {
  };
  private final DataHelper dataHelper;
  private final JsonManager mapper;

  @Inject
  public TokenManager(final DataHelper dataHelper,
                      final JsonManager mapper) {
    this.dataHelper = dataHelper;
    this.mapper = mapper;
  }

  public Token serialize(final Map<String, AttributeValue> map) {
    final Map<String, AttributeValue.Builder> serializedMap = map.entrySet().stream()
        .map(e -> Map.entry(e.getKey(), e.getValue().toBuilder()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    final String json = mapper.writeValue(serializedMap);
    final String base64 = dataHelper.toBase64(json);
    serializedMap.clear();
    return ImmutableToken.builder().value(base64).build();
  }

  public Map<String, AttributeValue> deserialize(final Token token) {
    final String json = dataHelper.toStringFromBase64(token.value());
    return mapper.readValue(json, TYPE_REFERENCE).entrySet().stream()
        .map(e -> Map.entry(e.getKey(), e.getValue().build()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

}
