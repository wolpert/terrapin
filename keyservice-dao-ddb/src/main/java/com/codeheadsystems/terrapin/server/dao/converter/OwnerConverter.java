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
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.OwnerIdentifier;
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
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;
import software.amazon.awssdk.utils.ImmutableMap;

@Singleton
public class OwnerConverter {

    public static final String HASH = "owner:%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(OwnerConverter.class);
    private final TableConfiguration configuration;

    @Inject
    public OwnerConverter(final TableConfiguration configuration) {
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

    public QueryRequest toOwnerQueryRequest(final OwnerIdentifier identifier) {
        LOGGER.debug("toOwnerQueryRequest({})", identifier);
        return QueryRequest.builder()
                .tableName(configuration.tableName())
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .scanIndexForward(false) // reverse the result set
                .limit(1)
                .keyConditions(Map.of(configuration.hashKey(), Condition.builder()
                        .comparisonOperator(ComparisonOperator.EQ)
                        .attributeValueList(fromS(String.format(HASH, identifier.owner())))
                        .build()))
                .build();
    }

    private String getRangeKey(final KeyIdentifier identifier) {
        return identifier.key();
    }

    private String getOwnerHashKey(final OwnerIdentifier identifier) {
        return String.format(HASH, identifier.owner());
    }

}
