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

import com.codeheadsystems.keystore.server.dao.ddb.configuration.TableConfiguration;
import com.codeheadsystems.keystore.server.dao.ddb.converter.KeyConverter;
import com.codeheadsystems.keystore.server.dao.ddb.converter.OwnerConverter;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveSpecification;
import software.amazon.awssdk.services.dynamodb.model.UpdateTimeToLiveRequest;

/**
 * Used for programmatic management of the table.
 * Note, in production, you should use CDK to manage instead of this. This class
 * was built mostly for testing purposes. Remember to encrypt the table with keys
 * as required by your organization.
 */
@Singleton
public class AwsManager {

  private final DynamoDbClient client;
  private final TableConfiguration tableConfiguration;

  /**
   * Instantiates a new Aws manager.
   *
   * @param client             the client
   * @param tableConfiguration the table configuration
   */
  @Inject
  public AwsManager(final DynamoDbClient client,
                    final TableConfiguration tableConfiguration) {
    this.client = client;
    this.tableConfiguration = tableConfiguration;
  }

  /**
   * Create table request create table request.
   *
   * @return the create table request
   */
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
            .attributeName(KeyConverter.ACTIVE_HASH).attributeType(ScalarAttributeType.S).build(),
        AttributeDefinition.builder()
            .attributeName(KeyConverter.OWNER_HASH_KEY_VERSION_IDX).attributeType(ScalarAttributeType.S).build(),
        AttributeDefinition.builder()
            .attributeName(OwnerConverter.OWNER_SEARCH_IDX).attributeType(ScalarAttributeType.S).build()
    );

    // Index for active keys for a version.
    final GlobalSecondaryIndex activeIndex = GlobalSecondaryIndex.builder()
        .indexName(tableConfiguration.activeIndex())
        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
        .keySchema(
            KeySchemaElement.builder().keyType(KeyType.HASH).attributeName(KeyConverter.ACTIVE_HASH).build(),
            KeySchemaElement.builder().keyType(KeyType.RANGE).attributeName(tableConfiguration.rangeKey()).build()
        ).build();

    // Index for all keys of an owner.
    final GlobalSecondaryIndex ownerIndex = GlobalSecondaryIndex.builder()
        .indexName(tableConfiguration.ownerIndex())
        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
        .keySchema(
            KeySchemaElement.builder().keyType(KeyType.HASH)
                .attributeName(KeyConverter.OWNER_HASH_KEY_VERSION_IDX).build(),
            KeySchemaElement.builder().keyType(KeyType.RANGE)
                .attributeName(tableConfiguration.hashKey()).build()
        ).build();

    // Index for all owners.
    final GlobalSecondaryIndex ownerSearchIndex = GlobalSecondaryIndex.builder()
        .indexName(tableConfiguration.ownerSearchIndex())
        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
        .keySchema(
            KeySchemaElement.builder().keyType(KeyType.HASH).attributeName(OwnerConverter.OWNER_SEARCH_IDX).build(),
            KeySchemaElement.builder().keyType(KeyType.RANGE).attributeName(tableConfiguration.hashKey()).build()
        ).build();

    return CreateTableRequest.builder()
        .tableName(tableConfiguration.tableName())
        .billingMode(BillingMode.PAY_PER_REQUEST)
        .keySchema(hashKey, rangeKey)
        .attributeDefinitions(attributeDefinitions)
        .globalSecondaryIndexes(activeIndex, ownerIndex, ownerSearchIndex)
        .build();
  }


  /**
   * Create table.
   */
  public void createTable() {
    client.createTable(createTableRequest());
    client.updateTimeToLive(updateTimeToLiveRequest());
  }

  /**
   * Update time to live request update time to live request.
   *
   * @return the update time to live request
   */
  protected UpdateTimeToLiveRequest updateTimeToLiveRequest() {
    return UpdateTimeToLiveRequest.builder()
        .tableName(tableConfiguration.tableName())
        .timeToLiveSpecification(TimeToLiveSpecification.builder()
            .attributeName(tableConfiguration.ttlKey()).enabled(true).build())
        .build();
  }

}
