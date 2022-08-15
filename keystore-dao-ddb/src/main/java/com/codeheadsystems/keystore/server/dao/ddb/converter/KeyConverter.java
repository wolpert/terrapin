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

import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromB;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromBool;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromN;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS;

import com.codeheadsystems.keystore.server.dao.ddb.configuration.TableConfiguration;
import com.codeheadsystems.keystore.server.dao.ddb.manager.TokenManager;
import com.codeheadsystems.keystore.server.dao.model.Batch;
import com.codeheadsystems.keystore.server.dao.model.ImmutableBatch;
import com.codeheadsystems.keystore.server.dao.model.ImmutableKey;
import com.codeheadsystems.keystore.server.dao.model.ImmutableKeyVersionIdentifier;
import com.codeheadsystems.keystore.server.dao.model.Key;
import com.codeheadsystems.keystore.server.dao.model.KeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.KeyVersionIdentifier;
import com.codeheadsystems.keystore.server.dao.model.Token;
import com.codeheadsystems.keystore.server.exception.DatalayerException;
import com.codeheadsystems.metrics.Metrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Date;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;
import software.amazon.awssdk.utils.ImmutableMap;

/**
 * Converter for keys.
 */
@Singleton
public class KeyConverter {

  /**
   * The data that makes up the key itself; used for encryption/decryption.
   */
  public static final String KEY_VALUE = "key_value";

  /**
   * Is this key version active.
   */
  public static final String ACTIVE = "active";
  /**
   * What type of key is it.
   */
  public static final String TYPE = "type";
  public static final String CREATE = "create";
  public static final String UPDATE = "update";
  /**
   * The info record for a key. Parent object if you will.
   */
  public static final String INFO = "info";
  /**
   * Used to search for active key versions.
   */
  public static final String ACTIVE_HASH = "activeHashKey";
  /**
   * Used to search for all key versions that belong to an owner.
   */
  public static final String OWNER_HASH_KEY_VERSION_IDX = "ownerHashKeyVersion";
  /**
   * The format for the hashkey for a key.
   */
  public static final String KEY_VERSION_HASH = "keyVersion:%s:%s";
  public static final String INVALID_INDEX = "invalid.index";
  public static final String KEYCONVERTER_ACTIVEINDEX = "keyconverter.activeindex";
  public static final String MISSING_BUT_EXPECTED = "missing.but.expected";
  public static final String FOUND_UNEXPECTEDLY = "found.unexpectedly";
  private static final Logger LOGGER = LoggerFactory.getLogger(KeyConverter.class);
  private final TableConfiguration configuration;
  private final TokenManager tokenManager;
  private final Counter activeWithoutIndexCounter;
  private final Counter inactiveWithIndexCounter;

  /**
   * Default constructor.
   *
   * @param configuration for table configuration.
   * @param metrics for reporting.
   * @param tokenManager for batch conversion.
   */
  @Inject
  public KeyConverter(final TableConfiguration configuration,
                      final Metrics metrics,
                      final TokenManager tokenManager) {
    LOGGER.info("KeyConverter({})", configuration);
    this.configuration = configuration;
    this.tokenManager = tokenManager;
    final MeterRegistry registry = metrics.registry();
    activeWithoutIndexCounter = registry.counter(KEYCONVERTER_ACTIVEINDEX, INVALID_INDEX, MISSING_BUT_EXPECTED);
    inactiveWithIndexCounter = registry.counter(KEYCONVERTER_ACTIVEINDEX, INVALID_INDEX, FOUND_UNEXPECTEDLY);
  }

  /**
   * Creates a put item request.
   *
   * @param key for the request
   * @return a put item request.
   */
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
    builder.put(OWNER_HASH_KEY_VERSION_IDX, fromS(key.keyVersionIdentifier().owner()));
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

  /**
   * Creates a get item request.
   *
   * @param identifier to convert.
   * @return the request.
   */
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

  /**
   * Converts a response to a key.
   *
   * @param response from ddb.
   * @return a key.
   */
  public Key from(final GetItemResponse response) {
    LOGGER.debug("GetItemResponse()"); // don't log the response since it has the key itself
    final Map<String, AttributeValue> item = response.item();
    return from(item);
  }

  /**
   * Converts a response to a key.
   *
   * @param item from ddb.
   * @return a key.
   */
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
    if (tokens.length != 3) {
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

  /**
   * Gets a query request for active keys.
   *
   * @param identifier for the request.
   * @return the request.
   */
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


  /**
   * Gets a query request for active keys.
   *
   * @param identifier for the request.
   * @param nextToken token to use if set.
   * @return the request.
   */
  public QueryRequest toKeyVersionsQueryRequest(final KeyIdentifier identifier,
                                                final Token nextToken) {
    LOGGER.debug("toKeyVersionsQueryRequest({})", identifier);
    final QueryRequest.Builder builder = QueryRequest.builder()
        .tableName(configuration.tableName())
        .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
        .keyConditions(Map.of(configuration.hashKey(), Condition.builder()
            .comparisonOperator(ComparisonOperator.EQ)
            .attributeValueList(fromS(hashKey(identifier)))
            .build()))
        .attributesToGet(configuration.hashKey(), configuration.rangeKey()); // just the basics
    if (nextToken != null) {
      builder.exclusiveStartKey(tokenManager.deserialize(nextToken));
    }
    return builder.build();
  }

  /**
   * Creates a batch request from a query response for key versions.
   *
   * @param response from ddb.
   * @return a batch response.
   */
  public Batch<KeyVersionIdentifier> toBatchKeyVersionIdentifier(final QueryResponse response) {
    LOGGER.debug("toBatchKeyVersionIdentifier()");
    final ImmutableBatch.Builder<KeyVersionIdentifier> builder = ImmutableBatch.builder();
    if (response.hasItems()) { // get the key identifiers
      response.items().forEach(item -> builder.addList(versionIdentifierFrom(item)));
    }
    if (response.hasLastEvaluatedKey()) { // get the token.
      builder.nextToken(tokenManager.serialize(response.lastEvaluatedKey()));
    }
    return builder.build();
  }

  /**
   * Creates a delete request from a key identifier.
   *
   * @param identifier to delete.
   * @return the request.
   */
  public DeleteItemRequest toDeleteRequest(final KeyVersionIdentifier identifier) {
    LOGGER.debug("toDeleteRequest({})", identifier);
    final ImmutableMap.Builder<String, AttributeValue> builder = ImmutableMap.builder();
    builder.put(configuration.hashKey(), fromS(hashKey(identifier)));
    builder.put(configuration.rangeKey(), fromS(rangeKey(identifier)));
    return DeleteItemRequest.builder()
        .tableName(configuration.tableName())
        .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
        .key(builder.build())
        .build();
  }
}
