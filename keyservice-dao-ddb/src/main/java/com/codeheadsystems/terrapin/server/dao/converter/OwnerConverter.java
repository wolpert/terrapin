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

import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS;

import com.codeheadsystems.terrapin.server.dao.TableConfiguration;
import com.codeheadsystems.terrapin.server.dao.manager.SerializerManager;
import com.codeheadsystems.terrapin.server.dao.model.Batch;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableBatch;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.OwnerIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.Token;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;
import software.amazon.awssdk.utils.ImmutableMap;

@Singleton
public class OwnerConverter {

    public static final String HASH = "owner:%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(OwnerConverter.class);
    private final TableConfiguration configuration;
    private final SerializerManager serializerManager;

    @Inject
    public OwnerConverter(final TableConfiguration configuration,
                          final SerializerManager serializerManager) {
        this.serializerManager = serializerManager;
        LOGGER.info("OwnerConverter({})", configuration);
        this.configuration = configuration;
    }

    public PutItemRequest toPutItemRequest(final KeyIdentifier identifier) {
        LOGGER.debug("toPutItemRequest({})", identifier);
        final ImmutableMap.Builder<String, AttributeValue> builder = ImmutableMap.builder();
        final String hashKey = getOwnerHashKey(identifier);
        final String rangeKey = getRangeKey(identifier);
        builder.put(configuration.hashKey(), fromS(hashKey));
        builder.put(configuration.rangeKey(), fromS(rangeKey));
        return PutItemRequest.builder()
                .tableName(configuration.tableName())
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .item(builder.build())
                .build();
    }

    /**
     * returns a request to get the first (the newest by sort) record
     *
     * @param identifier
     * @return
     */
    public QueryRequest toOwnerQueryRequest(final OwnerIdentifier identifier) {
        LOGGER.debug("toOwnerQueryRequest({})", identifier);
        return toOwnerQueryKeysRequest(identifier, null).toBuilder()
                .limit(1)
                .scanIndexForward(false) // reverse the result set, the newest first.
                .build();

    }

    /**
     * returns a request to get the first (the newest by sort) record
     *
     * @param identifier to search for.
     * @param nextToken  can be null.
     * @return
     */
    public QueryRequest toOwnerQueryKeysRequest(final OwnerIdentifier identifier,
                                                final Token nextToken) {
        LOGGER.debug("toOwnerQueryKeysRequest({})", identifier);
        final QueryRequest.Builder builder = QueryRequest.builder()
                .tableName(configuration.tableName())
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .keyConditions(Map.of(configuration.hashKey(), Condition.builder()
                        .comparisonOperator(ComparisonOperator.EQ)
                        .attributeValueList(fromS(String.format(HASH, identifier.owner())))
                        .build()));
        if (nextToken != null) {
            builder.exclusiveStartKey(serializerManager.deserialize(nextToken));
        }
        return builder.build();
    }

    public Batch<KeyIdentifier> toBatchKeyIdentifier(final QueryResponse response) {
        LOGGER.debug("toBatchKeyIdentifier()");
        final ImmutableBatch.Builder<KeyIdentifier> builder = ImmutableBatch.builder();
        if (response.hasItems()) { // get the key identifiers
            response.items().forEach(item -> builder.addList(toKeyVersion(item)));
        }
        if (response.hasLastEvaluatedKey()) { // get the token.
            builder.nextToken(serializerManager.serialize(response.lastEvaluatedKey()));
        }
        return builder.build();
    }

    private KeyIdentifier toKeyVersion(final Map<String, AttributeValue> item) {
        return ImmutableKeyIdentifier.builder()
                .owner(getOwnerFrom(item.get(configuration.hashKey())))
                .key(item.get(configuration.rangeKey()).s())
                .build();
    }

    private String getOwnerFrom(final AttributeValue attributeValue) {
        final String hash = attributeValue.s();
        final String[] tokens = hash.split(":");
        return tokens[1];
    }

    private String getRangeKey(final KeyIdentifier identifier) {
        return identifier.key();
    }

    private String getOwnerHashKey(final OwnerIdentifier identifier) {
        return String.format(HASH, identifier.owner());
    }

}
