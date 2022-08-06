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

package com.codeheadsystems.terrapin.server.dao.accessor;

import static com.codeheadsystems.terrapin.server.dao.dagger.DDBModule.DDB_DAO_RETRY;

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.terrapin.server.dao.converter.BatchWriteConverter;
import com.codeheadsystems.terrapin.server.exception.DependencyException;
import com.codeheadsystems.terrapin.server.exception.RetryableException;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.Timer;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

/**
 * A wrapper around the DDBClient so that we can do retries, metrics, convert exceptions, etc.
 */
@Singleton
public class DynamoDbClientAccessor {

  public static final String DDB_ACCESSOR = "ddbAccessor.";
  public static final String PUT_ITEM_METRIC = DDB_ACCESSOR + "putItem";
  public static final String GET_ITEM_METRIC = DDB_ACCESSOR + "getItem";
  public static final String DELETE_ITEM_METRIC = DDB_ACCESSOR + "deleteItem";
  public static final String BATCH_WRITE_ITEM_METRIC = DDB_ACCESSOR + "batchWriteItem";
  private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbClientAccessor.class);
  private static final String QUERY_METRIC = DDB_ACCESSOR + "query";
  private final Metrics metrics;
  private final BatchWriteConverter batchWriteConverter;

  // --- function list ---
  private final Function<PutItemRequest, PutItemResponse> putItem;
  private final Function<GetItemRequest, GetItemResponse> getItem;
  private final Function<BatchWriteItemRequest, BatchWriteItemResponse> batchWriteItem;
  private final Function<QueryRequest, QueryResponse> query;
  private final Function<DeleteItemRequest, DeleteItemResponse> deleteItem;

  @Inject
  public DynamoDbClientAccessor(final DynamoDbClient dynamoDbClient,
                                final Metrics metrics,
                                final BatchWriteConverter batchWriteConverter,
                                @Named(DDB_DAO_RETRY) final Retry retry) {
    LOGGER.info("DynamoDbClientAccessor({},{},{})", dynamoDbClient, metrics, retry.getName());
    this.metrics = metrics;
    this.batchWriteConverter = batchWriteConverter;
    putItem = Retry.decorateFunction(retry,                  // retries
        (request) -> exceptionCheck(PUT_ITEM_METRIC,     // exception check and metrics
            () -> dynamoDbClient.putItem(request))); // the actual function
    getItem = Retry.decorateFunction(retry,
        (request) -> exceptionCheck(GET_ITEM_METRIC,
            () -> dynamoDbClient.getItem(request)));
    batchWriteItem = Retry.decorateFunction(retry,
        (request) -> exceptionCheck(BATCH_WRITE_ITEM_METRIC,
            () -> dynamoDbClient.batchWriteItem(request)));
    query = Retry.decorateFunction(retry,
        (request) -> exceptionCheck(QUERY_METRIC,
            () -> dynamoDbClient.query(request)));
    deleteItem = Retry.decorateFunction(retry,
        (request) -> exceptionCheck(DELETE_ITEM_METRIC,
            () -> dynamoDbClient.deleteItem(request)));
  }

  public BatchWriteItemResponse batchWriteItem(final BatchWriteItemRequest request) {
    return batchWriteItem.apply(request);
  }

  /**
   * Processes a request. Returns a non-empty request containing unprocessed items.
   */
  public Optional<BatchWriteItemRequest> batchWriteItemProcessor(final BatchWriteItemRequest request) {
    final BatchWriteItemResponse response = batchWriteItem(request);
    LOGGER.debug("batchWriteItemProcessor : " + response.consumedCapacity());
    return batchWriteConverter.unprocessedRequest(response);
  }

  public DeleteItemResponse deleteItem(final DeleteItemRequest request) {
    return deleteItem.apply(request);
  }

  public PutItemResponse putItem(final PutItemRequest request) {
    return putItem.apply(request);
  }

  public GetItemResponse getItem(final GetItemRequest request) {
    return getItem.apply(request);
  }

  public QueryResponse query(final QueryRequest request) {
    return query.apply(request);
  }

  private <T extends DynamoDbResponse> T exceptionCheck(final String metricName, final Supplier<T> supplier) {
    try {
      final Timer timer = metrics.registry().timer(metricName);
      return metrics.time(metricName, timer, supplier);
    } catch (ProvisionedThroughputExceededException | TransactionConflictException | RequestLimitExceededException |
             InternalServerErrorException e) {
      throw new RetryableException(e);
    } catch (RuntimeException e) {
      throw new DependencyException(e);
    }
  }
}
