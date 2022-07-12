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

package com.codeheadsystems.terrapin.server.dao;

import static com.codeheadsystems.terrapin.server.dao.converter.KeyConverter.ACTIVE_HASH;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

/**
 * Used for programmatic management of the table.
 * Note, in production, you should use CDK to manage instead of this. This class
 * was built mostly for testing purposes. Remember to encrypt the table with keys
 * as required by your organization.
 */
@Singleton
public class AWSManager {

    private final DynamoDbClient client;
    private final TableConfiguration tableConfiguration;

    @Inject
    public AWSManager(final DynamoDbClient client,
                      final TableConfiguration tableConfiguration) {
        this.client = client;
        this.tableConfiguration = tableConfiguration;
    }

    protected CreateTableRequest createTableRequest() {
        final KeySchemaElement hashKey = KeySchemaElement.builder()
                .keyType(KeyType.HASH).attributeName(tableConfiguration.hashKey()).build();
        final KeySchemaElement rangeKey = KeySchemaElement.builder()
                .keyType(KeyType.RANGE).attributeName(tableConfiguration.rangeKey()).build();
        final List<AttributeDefinition> attributeDefinitions = List.of(
                AttributeDefinition.builder()
                        .attributeName(tableConfiguration.hashKey()).attributeType(ScalarAttributeType.S).build(),
                AttributeDefinition.builder()
                        .attributeName(tableConfiguration.rangeKey()).attributeType(ScalarAttributeType.S).build(),
                AttributeDefinition.builder()
                        .attributeName(ACTIVE_HASH).attributeType(ScalarAttributeType.S).build()
        );

        final GlobalSecondaryIndex activeIndex = GlobalSecondaryIndex.builder()
                .indexName(tableConfiguration.activeIndex())
                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                .keySchema(
                        KeySchemaElement.builder().keyType(KeyType.HASH).attributeName(ACTIVE_HASH).build(),
                        KeySchemaElement.builder().keyType(KeyType.RANGE).attributeName(tableConfiguration.rangeKey()).build()
                ).build();

        return CreateTableRequest.builder()
                .tableName(tableConfiguration.tableName())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .keySchema(hashKey, rangeKey)
                .attributeDefinitions(attributeDefinitions)
                .globalSecondaryIndexes(activeIndex)
                .build();
    }


    public void createTable() {
        client.createTable(createTableRequest());
        client.updateTimeToLive(updateTimeToLiveRequest());
    }

    protected UpdateTimeToLiveRequest updateTimeToLiveRequest() {
        return UpdateTimeToLiveRequest.builder()
                .tableName(tableConfiguration.tableName())
                .timeToLiveSpecification(TimeToLiveSpecification.builder()
                        .attributeName(tableConfiguration.ttlKey()).enabled(true).build())
                .build();
    }

}
