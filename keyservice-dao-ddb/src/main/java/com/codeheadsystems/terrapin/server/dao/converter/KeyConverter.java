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

import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromB;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromBool;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromN;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS;

import com.codeheadsystems.terrapin.server.dao.TableConfiguration;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKey;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import java.util.Date;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;
import software.amazon.awssdk.utils.ImmutableMap;

@Singleton
public class KeyConverter {

    public static final String KEY_VALUE = "key_value";
    public static final String ACTIVE = "active";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String TYPE = "type";
    public static final String ACTIVE_HASH = "activeHashKey";
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyConverter.class);
    private final TableConfiguration configuration;

    @Inject
    public KeyConverter(final TableConfiguration configuration) {
        this.configuration = configuration;
        LOGGER.info("KeyConverter({})", configuration);
    }

    public PutItemRequest toPutItemRequest(final Key key) {
        final KeyVersionIdentifier identifier = key.keyIdentifier();
        LOGGER.debug("toPutItemRequest({})", identifier);
        final ImmutableMap.Builder<String, AttributeValue> builder = ImmutableMap.builder();
        final String hashKey = hashKey(identifier);
        builder.put(configuration.hashKey(), fromS(hashKey));
        builder.put(configuration.rangeKey(), fromS(rangeKey(identifier)));
        builder.put(KEY_VALUE, fromB(SdkBytes.fromByteArray(key.value())));
        builder.put(TYPE, fromS(key.type()));
        builder.put(ACTIVE, fromBool(key.active()));
        builder.put(CREATE, fromN(Long.toString(key.createDate().getTime())));
        builder.put(ACTIVE_HASH, key.active() ? fromS(hashKey) : null); // index
        key.updateDate().ifPresent(date -> builder.put(UPDATE, fromN(Long.toString(date.getTime()))));
        return PutItemRequest.builder()
                .tableName(configuration.tableName())
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .item(builder.build())
                .build();
    }

    private String rangeKey(final KeyVersionIdentifier identifier) {
        return identifier.version().toString();
    }

    private String hashKey(final KeyVersionIdentifier identifier) {
        return String.format("%s:%s", identifier.owner(), identifier.key());
    }

    public GetItemRequest toGetItemRequest(final KeyVersionIdentifier identifier) {
        LOGGER.debug("toGetItemRequest({})", identifier);
        final ImmutableMap.Builder<String, AttributeValue> builder = ImmutableMap.builder();
        builder.put(configuration.hashKey(), fromS(hashKey(identifier)));
        builder.put(configuration.rangeKey(), fromS(rangeKey(identifier)));
        final GetItemRequest request = GetItemRequest.builder()
                .tableName(configuration.tableName())
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .key(builder.build())
                .build();
        return request;

    }

    public Key from(final GetItemResponse response) {
        LOGGER.debug("GetItemResponse()"); // don't log the response since it has the key itself
        final Map<String, AttributeValue> item = response.item();
        final ImmutableKey.Builder builder = ImmutableKey.builder()
                .keyIdentifier(versionIdentifierFrom(item))
                .value(item.get(KEY_VALUE).b().asByteArray())
                .active(item.get(ACTIVE).bool())
                .type(item.get(TYPE).s())
                .createDate(new Date(Long.parseLong(item.get(CREATE).n())));
        if (item.containsKey(UPDATE)) {
            builder.updateDate(new Date(Long.parseLong(item.get(CREATE).n())));
        }
        final Key key = builder.build();
        // Verification
        final boolean hasActiveHash = item.containsKey(ACTIVE_HASH);
        if (hasActiveHash && !key.active()) {
            LOGGER.error("Key is listed as active by not searchable that way! {}", key.keyIdentifier());
        } else if (!hasActiveHash && key.active()) {
            LOGGER.error("Key is searchable as active but is itself not active! {}", key.keyIdentifier());
        }
        return key;
    }

    private KeyVersionIdentifier versionIdentifierFrom(final Map<String, AttributeValue> item) {
        final String hash = item.get(configuration.hashKey()).s();
        final String[] tokens = hash.split(":");
        final String owner = tokens[0];
        final String key = tokens[1];
        return ImmutableKeyVersionIdentifier.builder()
                .owner(owner)
                .key(key)
                .version(Long.parseLong(item.get(configuration.rangeKey()).s()))
                .build();
    }
}
