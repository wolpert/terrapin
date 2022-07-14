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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromB;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromBool;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromN;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS;

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.terrapin.common.factory.ObjectMapperFactory;
import com.codeheadsystems.terrapin.server.dao.ImmutableTableConfiguration;
import com.codeheadsystems.terrapin.server.dao.TableConfiguration;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.utils.ImmutableMap;

@ExtendWith(MockitoExtension.class)
public class KeyConverterTest {

    public static final String HASHKEY = "owner:key";
    public static final String RANGEKEY = "10";
    private static final TableConfiguration TABLE_CONFIGURATION = ImmutableTableConfiguration.builder().build();
    @Mock private Metrics metrics;
    @Mock private MeterRegistry registry;
    @Mock private Counter activeCounter;
    @Mock private Counter inactiveCounter;

    private KeyConverter converter;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        when(metrics.registry()).thenReturn(registry);
        when(registry.counter(KEYCONVERTER_ACTIVEINDEX, INVALID_INDEX, MISSING_BUT_EXPECTED)).thenReturn(activeCounter);
        when(registry.counter(KEYCONVERTER_ACTIVEINDEX, INVALID_INDEX, FOUND_UNEXPECTEDLY)).thenReturn(inactiveCounter);
        converter = new KeyConverter(TABLE_CONFIGURATION, metrics);
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
                        ACTIVE_HASH,
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
                        ACTIVE_HASH,
                        CREATE);
        ;
    }

    @Test
    void fromRequest() {
        final ImmutableMap.Builder<String, AttributeValue> builder = ImmutableMap.builder();
        builder.put(TABLE_CONFIGURATION.hashKey(), fromS(HASHKEY));
        builder.put(TABLE_CONFIGURATION.rangeKey(), fromS(RANGEKEY));
        builder.put(KEY_VALUE, fromB(SdkBytes.fromByteArray(new byte[]{0, 1, 2})));
        builder.put(TYPE, fromS("type"));
        builder.put(ACTIVE, fromBool(true));
        builder.put(CREATE, fromN(Long.toString(100)));
        builder.put(ACTIVE_HASH, fromS(HASHKEY)); // index
        final GetItemResponse response = GetItemResponse.builder()
                .item(builder.build())
                .build();
        final Key generatedKey = converter.from(response);

        assertThat(generatedKey)
                .isNotNull()
                .hasFieldOrPropertyWithValue("keyVersionIdentifier",
                        ImmutableKeyVersionIdentifier.builder().owner("owner").key("key").version(10L).build())
                .hasFieldOrPropertyWithValue("active", true);
        verify(activeCounter).increment(0);
        verify(inactiveCounter).increment(0);
    }

    @Test
    void fromRequest_inactiveKey_withIndex() {
        final ImmutableMap.Builder<String, AttributeValue> builder = ImmutableMap.builder();
        builder.put(TABLE_CONFIGURATION.hashKey(), fromS(HASHKEY));
        builder.put(TABLE_CONFIGURATION.rangeKey(), fromS(RANGEKEY));
        builder.put(KEY_VALUE, fromB(SdkBytes.fromByteArray(new byte[]{0, 1, 2})));
        builder.put(TYPE, fromS("type"));
        builder.put(ACTIVE, fromBool(false));
        builder.put(CREATE, fromN(Long.toString(100)));
        builder.put(ACTIVE_HASH, fromS(HASHKEY)); // index
        final GetItemResponse response = GetItemResponse.builder()
                .item(builder.build())
                .build();
        final Key generatedKey = converter.from(response);

        assertThat(generatedKey)
                .isNotNull()
                .hasFieldOrPropertyWithValue("keyVersionIdentifier",
                        ImmutableKeyVersionIdentifier.builder().owner("owner").key("key").version(10L).build())
                .hasFieldOrPropertyWithValue("active", false);
        verify(activeCounter).increment(0);
        verify(inactiveCounter).increment(1);

    }

    @Test
    void fromRequest_activeKey_noIndex() {
        final ImmutableMap.Builder<String, AttributeValue> builder = ImmutableMap.builder();
        builder.put(TABLE_CONFIGURATION.hashKey(), fromS(HASHKEY));
        builder.put(TABLE_CONFIGURATION.rangeKey(), fromS(RANGEKEY));
        builder.put(KEY_VALUE, fromB(SdkBytes.fromByteArray(new byte[]{0, 1, 2})));
        builder.put(TYPE, fromS("type"));
        builder.put(ACTIVE, fromBool(true));
        builder.put(CREATE, fromN(Long.toString(100)));
        final GetItemResponse response = GetItemResponse.builder()
                .item(builder.build())
                .build();
        final Key generatedKey = converter.from(response);

        assertThat(generatedKey)
                .isNotNull()
                .hasFieldOrPropertyWithValue("keyVersionIdentifier",
                        ImmutableKeyVersionIdentifier.builder().owner("owner").key("key").version(10L).build())
                .hasFieldOrPropertyWithValue("active", true);
        verify(activeCounter).increment(1);
        verify(inactiveCounter).increment(0);

    }
}