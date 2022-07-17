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

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.terrapin.common.factory.ObjectMapperFactory;
import com.codeheadsystems.terrapin.common.helper.DataHelper;
import com.codeheadsystems.terrapin.common.manager.JsonManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class SerializerManagerTest {

    private static final String KEY = "KEY";
    private static final String OWNER = "OWNER";
    private static final AttributeValue VALUE = AttributeValue.builder().s(OWNER).build();
    private static final Map<String, AttributeValue> MAP = Map.of(KEY, VALUE);

    private SerializerManager serializerManager;

    @BeforeEach
    void setup() {
        final ObjectMapper objectMapper = new ObjectMapperFactory().generate();
        final JsonManager jsonManager = new JsonManager(objectMapper);
        final DataHelper dataHelper = new DataHelper();
        serializerManager = new SerializerManager(dataHelper, jsonManager);
    }

    @Test
    public void roundTrip() {
        final String token = serializerManager.serialize(MAP);
        final Map<String, AttributeValue> result = serializerManager.deserialize(token);
        assertThat(result)
                .isEqualTo(MAP);
    }

}