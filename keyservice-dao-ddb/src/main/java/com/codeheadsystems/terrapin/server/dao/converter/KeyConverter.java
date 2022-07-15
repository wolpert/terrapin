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

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.terrapin.server.dao.TableConfiguration;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKey;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.exception.DatalayerException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Date;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.ImmutableMap;

@Singleton
public class KeyConverter {

    public static final String KEY_VALUE = "key_value";
    public static final String ACTIVE = "active";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String TYPE = "type";
    public static final String ACTIVE_HASH = "activeHashKey";
    public static final String OWNER_HASH = "ownerHashKey";
    public static final String INVALID_INDEX = "invalid.index";
    public static final String KEYCONVERTER_ACTIVEINDEX = "keyconverter.activeindex";
    public static final String MISSING_BUT_EXPECTED = "missing.but.expected";
    public static final String FOUND_UNEXPECTEDLY = "found.unexpectedly";
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyConverter.class);
    public static final String KEY_VERSION_HASH = "keyVersion:%s:%s";
    private final TableConfiguration configuration;
    private final Counter activeWithoutIndexCounter;
    private final Counter inactiveWithIndexCounter;

    @Inject
    public KeyConverter(final TableConfiguration configuration,
                        final Metrics metrics) {
        this.configuration = configuration;
        final MeterRegistry registry = metrics.registry();
        activeWithoutIndexCounter = registry.counter(KEYCONVERTER_ACTIVEINDEX, INVALID_INDEX, MISSING_BUT_EXPECTED);
        inactiveWithIndexCounter = registry.counter(KEYCONVERTER_ACTIVEINDEX, INVALID_INDEX, FOUND_UNEXPECTEDLY);
        LOGGER.info("KeyConverter({})", configuration);
    }

    public PutItemRequest toPutItemRequest(final Key key) {
        final KeyVersionIdentifier identifier = key.keyVersionIdentifier();
        LOGGER.debug("toPutItemRequest({})", identifier);
        final ImmutableMap.Builder<String, AttributeValue> builder = ImmutableMap.builder();
        final String hashKey = hashKey(identifier);
        builder.put(configuration.hashKey(), fromS(hashKey));
        builder.put(configuration.rangeKey(), fromS(rangeKey(identifier)));
        builder.put(KEY_VALUE, fromB(SdkBytes.fromByteArray(key.value())));
        builder.put(TYPE, fromS(key.type()));
        builder.put(ACTIVE, fromBool(key.active()));
        builder.put(CREATE, fromN(Long.toString(key.createDate().getTime())));
        builder.put(OWNER_HASH, fromS(key.keyVersionIdentifier().owner()));
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

    private String hashKey(final KeyIdentifier identifier) {
        return String.format(KEY_VERSION_HASH, identifier.owner(), identifier.key());
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
        return from(item);
    }

    public Key from(final Map<String, AttributeValue> item) {
        final ImmutableKey.Builder builder = ImmutableKey.builder()
                .keyVersionIdentifier(versionIdentifierFrom(item))
                .value(item.get(KEY_VALUE).b().asByteArray())
                .active(item.get(ACTIVE).bool())
                .type(item.get(TYPE).s())
                .createDate(new Date(Long.parseLong(item.get(CREATE).n())));
        if (item.containsKey(UPDATE)) {
            builder.updateDate(new Date(Long.parseLong(item.get(CREATE).n())));
        }
        final Key key = builder.build();
        // Verification
        verifyActiveKeyIndex(item, key);
        return key;
    }

    private void verifyActiveKeyIndex(final Map<String, AttributeValue> item,
                                      final Key key) {
        final boolean hasActiveHash = item.containsKey(ACTIVE_HASH);
        if (hasActiveHash && !key.active()) {
            LOGGER.error("Key is listed as active by not searchable that way! {}", key.keyVersionIdentifier());
            inactiveWithIndexCounter.increment(1);
            activeWithoutIndexCounter.increment(0);
        } else if (!hasActiveHash && key.active()) {
            LOGGER.error("Key is searchable as active but is itself not active! {}", key.keyVersionIdentifier());
            activeWithoutIndexCounter.increment(1);
            inactiveWithIndexCounter.increment(0);
        } else {
            activeWithoutIndexCounter.increment(0);
            inactiveWithIndexCounter.increment(0);
        }
    }

    private KeyVersionIdentifier versionIdentifierFrom(final Map<String, AttributeValue> item) {
        final String hash = item.get(configuration.hashKey()).s();
        final String[] tokens = hash.split(":");
        if (tokens.length!=3) {
            LOGGER.error("Token lookup failed, badly encoded: {}", hash);
            throw new DatalayerException("Token lookup has incorrect length: " + hash);
        }
        final String owner = tokens[1];
        final String key = tokens[2];
        return ImmutableKeyVersionIdentifier.builder()
                .owner(owner)
                .key(key)
                .version(Long.parseLong(item.get(configuration.rangeKey()).s()))
                .build();
    }

    public QueryRequest toActiveQueryRequest(final KeyIdentifier identifier) {
        LOGGER.debug("toActiveQueryRequest({})", identifier);
        return QueryRequest.builder()
                .tableName(configuration.tableName())
                .indexName(configuration.activeIndex())
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .scanIndexForward(false) // reverse the result set
                .limit(1) // this actually works because we are using the index, and will only get the first result.
                .keyConditions(Map.of(ACTIVE_HASH, Condition.builder()
                        .comparisonOperator(ComparisonOperator.EQ)
                        .attributeValueList(fromS(hashKey(identifier)))
                        .build()))
                .build();
    }

    public QueryRequest toOwnerQueryRequest(final String owner) {
        LOGGER.debug("toOwnerQueryRequest({})", owner);
        return QueryRequest.builder()
                .tableName(configuration.tableName())
                .indexName(configuration.ownerIndex())
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .scanIndexForward(false) // reverse the result set
                .limit(1) // this actually works because we are using the index, and will only get the first result.
                .keyConditions(Map.of(OWNER_HASH, Condition.builder()
                        .comparisonOperator(ComparisonOperator.EQ)
                        .attributeValueList(fromS(owner))
                        .build()))
                .build();
    }

}
