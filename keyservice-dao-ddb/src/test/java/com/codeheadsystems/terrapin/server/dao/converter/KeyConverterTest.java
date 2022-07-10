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

package com.codeheadsystems.terrapin.server.dao.converter;

import static com.codeheadsystems.terrapin.server.dao.converter.KeyConverter.*;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;

import com.codeheadsystems.terrapin.common.factory.ObjectMapperFactory;
import com.codeheadsystems.terrapin.server.dao.ImmutableTableConfiguration;
import com.codeheadsystems.terrapin.server.dao.TableConfiguration;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

class KeyConverterTest {

    private static final TableConfiguration TABLE_CONFIGURATION = ImmutableTableConfiguration.builder().build();

    private KeyConverter converter;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        converter = new KeyConverter(TABLE_CONFIGURATION);
        objectMapper = new ObjectMapperFactory().generate();
    }

    @Test
    void toPutItemRequest() throws IOException {
        final InputStream stream = KeyConverterTest.class.getClassLoader().getResourceAsStream("fixture/Key.json");
        final Key key = objectMapper.readValue(stream, Key.class);
        final PutItemRequest request = converter.toPutItemRequest(key);
        assertThat(request)
                .isNotNull()
                .hasFieldOrPropertyWithValue("tableName", TABLE_CONFIGURATION.tableName())
                .extracting("item", as(map(String.class, AttributeValue.class)))
                .containsOnlyKeys(TABLE_CONFIGURATION.hashKey(),
                        TABLE_CONFIGURATION.rangeKey(),
                        KEY_VALUE,
                        ACTIVE,
                        TYPE,
                        CREATE,
                        UPDATE);
        ;
    }

    @Test
    void toPutItemRequest_noUpdate() throws IOException {
        final InputStream stream = KeyConverterTest.class.getClassLoader().getResourceAsStream("fixture/KeyNoUpdate.json");
        final Key key = objectMapper.readValue(stream, Key.class);
        final PutItemRequest request = converter.toPutItemRequest(key);
        assertThat(request)
                .isNotNull()
                .hasFieldOrPropertyWithValue("tableName", TABLE_CONFIGURATION.tableName())
                .extracting("item", as(map(String.class, AttributeValue.class)))
                .containsOnlyKeys(TABLE_CONFIGURATION.hashKey(),
                        TABLE_CONFIGURATION.rangeKey(),
                        KEY_VALUE,
                        TYPE,
                        ACTIVE,
                        CREATE);
        ;
    }

}