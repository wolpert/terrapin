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

package com.codeheadsystems.terrapin.server.dao.manager;

import com.codeheadsystems.terrapin.common.helper.DataHelper;
import com.codeheadsystems.terrapin.common.manager.JsonManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Singleton
public class SerializerManager {

    public static final TypeReference<HashMap<String, AttributeValue.Builder>> TYPE_REFERENCE = new TypeReference<>() {
    };
    private final DataHelper dataHelper;
    private final JsonManager mapper;

    @Inject
    public SerializerManager(final DataHelper dataHelper,
                             final JsonManager mapper) {
        this.dataHelper = dataHelper;
        this.mapper = mapper;
    }

    public String serialize(final Map<String, AttributeValue> map) {
        final HashMap<String, AttributeValue.Builder> serializedMap = new HashMap<>();
        map.forEach((k, v) -> serializedMap.put(k, v.toBuilder()));
        final String json = mapper.writeValue(serializedMap);
        return dataHelper.toBase64(json);
    }

    public Map<String, AttributeValue> deserialize(final String base64) {
        final String json = dataHelper.toStringFromBase64(base64);
        final HashMap<String, AttributeValue.Builder> serializedMap = mapper.readValue(json, TYPE_REFERENCE);
        final HashMap<String, AttributeValue> map = new HashMap<>();
        serializedMap.forEach((k, v) -> map.put(k, v.build()));
        return map;
    }

}
