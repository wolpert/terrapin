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

package com.codeheadsystems.keystore.server.dao.ddb.converter;

import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS;

import com.codeheadsystems.keystore.server.dao.ddb.configuration.TableConfiguration;
import com.codeheadsystems.keystore.server.dao.ddb.manager.TokenManager;
import com.codeheadsystems.keystore.server.dao.model.Batch;
import com.codeheadsystems.keystore.server.dao.model.ImmutableBatch;
import com.codeheadsystems.keystore.server.dao.model.ImmutableKeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.ImmutableOwnerIdentifier;
import com.codeheadsystems.keystore.server.dao.model.KeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.OwnerIdentifier;
import com.codeheadsystems.keystore.server.dao.model.Token;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;
import software.amazon.awssdk.utils.ImmutableMap;

/**
 * Converter for ownesr.
 */
@Singleton
public class OwnerConverter {

  /**
   * The constant HASH.
   */
  public static final String HASH = "owner:%s";
  /**
   * The constant INFO_RANGE.
   */
  public static final String INFO_RANGE = "info";
  /**
   * The constant OWNER_SEARCH_IDX.
   */
  public static final String OWNER_SEARCH_IDX = "ownerSearchIdx";
  /**
   * The constant KEY_RANGE_FORMAT.
   */
  public static final String KEY_RANGE_FORMAT = "key:%s";
  private static final Logger LOGGER = LoggerFactory.getLogger(OwnerConverter.class);
  private final TableConfiguration configuration;
  private final TokenManager tokenManager;

  /**
   * Default constructor.
   *
   * @param configuration for the db.
   * @param tokenManager  token manager for conversions.
   */
  @Inject
  public OwnerConverter(final TableConfiguration configuration,
                        final TokenManager tokenManager) {
    LOGGER.info("OwnerConverter({})", configuration);
    this.tokenManager = tokenManager;
    this.configuration = configuration;
  }

  /**
   * Creates a put item request.
   *
   * @param identifier for the request.
   * @return the request.
   */
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
   * Creates a put item request.
   *
   * @param identifier for the request.
   * @return the request.
   */
  public PutItemRequest toOwnerPutItemRequest(final OwnerIdentifier identifier) {
    LOGGER.debug("toOwnerPutItemRequest({})", identifier);
    final ImmutableMap.Builder<String, AttributeValue> builder = ImmutableMap.builder();
    final String hashKey = getOwnerHashKey(identifier);
    builder.put(configuration.hashKey(), fromS(hashKey));
    builder.put(configuration.rangeKey(), fromS(INFO_RANGE));
    builder.put(OWNER_SEARCH_IDX, fromS(INFO_RANGE)); // used to search for all owners.
    return PutItemRequest.builder()
        .tableName(configuration.tableName())
        .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
        .item(builder.build())
        .build();
  }

  /**
   * Creates a get item request.
   *
   * @param identifier for the request.
   * @return the request.
   */
  public GetItemRequest toOwnerGetItemRequest(final OwnerIdentifier identifier) {
    LOGGER.debug("toOwnerGetItemRequest({})", identifier);
    final ImmutableMap.Builder<String, AttributeValue> builder = ImmutableMap.builder();
    builder.put(configuration.hashKey(), fromS(getOwnerHashKey(identifier)));
    builder.put(configuration.rangeKey(), fromS(INFO_RANGE));
    return GetItemRequest.builder()
        .tableName(configuration.tableName())
        .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
        .key(builder.build())
        .build();
  }

  /**
   * Creates an owner from the item.
   *
   * @param item from the response.
   * @return an identifier.
   */
  public OwnerIdentifier toOwnerIdentifier(final Map<String, AttributeValue> item) {
    return ImmutableOwnerIdentifier.builder()
        .owner(getOwnerFrom(item.get(configuration.hashKey())))
        .build();
  }

  /**
   * returns a request to get the first (the newest by sort) record.
   *
   * @param identifier to search for.
   * @param nextToken  can be null.
   * @return the query request.
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
      builder.exclusiveStartKey(tokenManager.deserialize(nextToken));
    }
    return builder.build();
  }

  /**
   * returns a request to get the owners in the system.
   *
   * @param nextToken can be null.
   * @return query request
   */
  public QueryRequest toOwnerSearchQueryRequest(final Token nextToken) {
    LOGGER.debug("toOwnerSearchQueryRequest()");
    final QueryRequest.Builder builder = QueryRequest.builder()
        .tableName(configuration.tableName())
        .indexName(configuration.ownerSearchIndex())
        .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
        .keyConditions(Map.of(OWNER_SEARCH_IDX, Condition.builder()
            .comparisonOperator(ComparisonOperator.EQ)
            .attributeValueList(fromS(INFO_RANGE))
            .build()));
    if (nextToken != null) {
      builder.exclusiveStartKey(tokenManager.deserialize(nextToken));
    }
    return builder.build();
  }

  /**
   * Creates a batch object.
   *
   * @param response from ddb.
   * @return the batch.
   */
  public Batch<KeyIdentifier> toBatchKeyIdentifier(final QueryResponse response) {
    LOGGER.debug("toBatchKeyIdentifier()");
    final ImmutableBatch.Builder<KeyIdentifier> builder = ImmutableBatch.builder();
    if (response.hasItems()) { // get the key identifiers
      // TODO: ERROR, this needs to ignore Owner INFO rows now.
      response.items().forEach(item -> builder.addList(toKeyIdentifier(item)));
    }
    if (response.hasLastEvaluatedKey()) { // get the token.
      builder.nextToken(tokenManager.serialize(response.lastEvaluatedKey()));
    }
    return builder.build();
  }

  /**
   * Creates a batch object.
   *
   * @param response from ddb.
   * @return the batch.
   */
  public Batch<OwnerIdentifier> toBatchOwnerIdentifier(final QueryResponse response) {
    LOGGER.debug("toBatchOwnerIdentifier()");
    final ImmutableBatch.Builder<OwnerIdentifier> builder = ImmutableBatch.builder();
    if (response.hasItems()) { // get the key identifiers
      response.items().forEach(item -> builder.addList(toOwnerIdentifier(item)));
    }
    if (response.hasLastEvaluatedKey()) { // get the token.
      builder.nextToken(tokenManager.serialize(response.lastEvaluatedKey()));
    }
    return builder.build();
  }

  private KeyIdentifier toKeyIdentifier(final Map<String, AttributeValue> item) {
    return ImmutableKeyIdentifier.builder()
        .owner(getOwnerFrom(item.get(configuration.hashKey())))
        .key(getKeyFrom(item.get(configuration.rangeKey())))
        .build();
  }

  private String getOwnerFrom(final AttributeValue attributeValue) {
    final String hash = attributeValue.s();
    final String[] tokens = hash.split(":");
    return tokens[1];
  }

  private String getKeyFrom(final AttributeValue attributeValue) {
    final String value = attributeValue.s();
    return value.substring(4); // length of 'key:'
  }

  private String getRangeKey(final KeyIdentifier identifier) {
    return String.format(KEY_RANGE_FORMAT, identifier.key());
  }

  private String getOwnerHashKey(final OwnerIdentifier identifier) {
    return String.format(HASH, identifier.owner());
  }

}
