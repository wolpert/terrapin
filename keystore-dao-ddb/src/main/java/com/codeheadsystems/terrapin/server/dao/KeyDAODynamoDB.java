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

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.terrapin.server.dao.accessor.DynamoDbClientAccessor;
import com.codeheadsystems.terrapin.server.dao.converter.BatchWriteConverter;
import com.codeheadsystems.terrapin.server.dao.converter.KeyConverter;
import com.codeheadsystems.terrapin.server.dao.converter.OwnerConverter;
import com.codeheadsystems.terrapin.server.dao.model.Batch;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableOwnerIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.OwnerIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.Token;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.*;

@Singleton
public class KeyDAODynamoDB implements KeyDAO {
    public static final String OWNER = "owner";
    public static final String PREFIX = "ddbdao.";
    public static final int MAX_TIMES_KEY_STORE = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyDAODynamoDB.class);
    private final DynamoDbClientAccessor dynamoDbClientAccessor;
    private final KeyConverter keyConverter;
    private final OwnerConverter ownerConverter;
    private final BatchWriteConverter batchWriteConverter;
    private final Metrics metrics;
    private final Counter counterKeyVersion;
    private final Counter counterKey;
    private final Counter counterOwner;
    private final Counter counterBatchWriteRanOut;

    @Inject
    public KeyDAODynamoDB(final DynamoDbClientAccessor dynamoDbClientAccessor,
                          final KeyConverter keyConverter,
                          final OwnerConverter ownerConverter,
                          final BatchWriteConverter batchWriteConverter,
                          final Metrics metrics) {
        LOGGER.info("KeyDAODynamoDB({},{},{})", dynamoDbClientAccessor, keyConverter, ownerConverter);
        this.batchWriteConverter = batchWriteConverter;
        this.dynamoDbClientAccessor = dynamoDbClientAccessor;
        this.keyConverter = keyConverter;
        this.ownerConverter = ownerConverter;
        this.metrics = metrics;
        final MeterRegistry registry = metrics.registry();
        counterKeyVersion = registry.counter(PREFIX + "found.key.version");
        counterKey = registry.counter(PREFIX + "found.key");
        counterOwner = registry.counter(PREFIX + "found.owner");
        counterBatchWriteRanOut = registry.counter(PREFIX + "batchWrite.ran.out");
    }

    private <T> T time(final String methodName,
                       final String owner,
                       final Supplier<T> supplier) {
        final String name = PREFIX + methodName;
        final Timer timer = metrics.registry().timer(name, OWNER, (owner == null ? "null" : owner)); // TODO: Vet cardinality. Set by configuration?
        return metrics.time(name, timer, supplier);
    }

    @Override
    public void store(final Key key) {
        LOGGER.debug("store({})", key.keyVersionIdentifier());
        time("storeKey", key.keyVersionIdentifier().owner(), () -> {
            final PutItemRequest keyPutItemRequest = keyConverter.toPutItemRequest(key);
            final PutItemRequest ownerPutItemRequest = ownerConverter.toPutItemRequest(key.keyVersionIdentifier());
            final BatchWriteItemRequest request = batchWriteConverter.fromPutItemRequests(keyPutItemRequest, ownerPutItemRequest);
            reProcessor(request, MAX_TIMES_KEY_STORE); // should not take this long for sure.
            return null;
        });
    }

    /**
     * This method will reprocess a batch write up to X times, as long as there are items that need processing.
     *
     * @param request  the request
     * @param maxTimes max times to process.
     */
    private void reProcessor(final BatchWriteItemRequest request,
                             final int maxTimes) {
        LOGGER.debug("reProcessor({}", maxTimes);
        time("reProcessor", null, () -> {
            Optional<BatchWriteItemRequest> nextRequest = Optional.of(request);
            int times = 0;
            do {
                times++;
                nextRequest = dynamoDbClientAccessor.batchWriteItemProcessor(nextRequest.get());
            } while (times < maxTimes && nextRequest.isPresent());
            counterBatchWriteRanOut.increment(nextRequest.isPresent() ? 1 : 0);
            nextRequest.ifPresent((n) -> {
                throw new IllegalStateException("Unable to fully process request:" + request.requestItems());
            });
            return null;
        });
    }

    @Override
    public OwnerIdentifier storeOwner(final String owner) {
        LOGGER.debug("storeOwner({})", owner);
        return time("storeOwner", owner, () -> {
            final OwnerIdentifier identifier = ImmutableOwnerIdentifier.builder().owner(owner).build();
            final PutItemRequest request = ownerConverter.toOwnerPutItemRequest(identifier);
            final PutItemResponse response = dynamoDbClientAccessor.putItem(request);
            final ConsumedCapacity consumedCapacity = response.consumedCapacity();
            LOGGER.debug("storeOwner:{}", consumedCapacity);
            return identifier;
        });
    }

    private void storeKey(final Key key) {
        LOGGER.debug("storeKey({})", key.keyVersionIdentifier());
        final PutItemRequest request = keyConverter.toPutItemRequest(key);
        final PutItemResponse response = dynamoDbClientAccessor.putItem(request);
        final ConsumedCapacity consumedCapacity = response.consumedCapacity();
        LOGGER.debug("storeKey:{}", consumedCapacity);
    }

    private void storeOwner(final Key key) {
        LOGGER.debug("storeOwner({})", key.keyVersionIdentifier());
        final PutItemRequest request = ownerConverter.toPutItemRequest(key.keyVersionIdentifier());
        final PutItemResponse response = dynamoDbClientAccessor.putItem(request);
        final ConsumedCapacity consumedCapacity = response.consumedCapacity();
        LOGGER.debug("storeOwner:{}", consumedCapacity);
    }

    @Override
    public Optional<Key> load(final KeyVersionIdentifier identifier) {
        LOGGER.debug("load({})", identifier);
        return time("loadKeyVersion", identifier.owner(), () -> {
            final GetItemRequest request = keyConverter.toGetItemRequest(identifier);
            final GetItemResponse response = dynamoDbClientAccessor.getItem(request);
            final ConsumedCapacity consumedCapacity = response.consumedCapacity();
            LOGGER.debug("load:{}", consumedCapacity);
            if (response.hasItem()) {
                counterKeyVersion.increment(1);
                return Optional.of(keyConverter.from(response));
            } else {
                counterKeyVersion.increment(0);
                return Optional.empty();
            }
        });
    }

    /**
     * Query against the active hash, returning the key with the greatest number.
     * Empty optional if there is no active key or if there is no keys in general.
     */
    @Override
    public Optional<Key> load(final KeyIdentifier identifier) {
        LOGGER.debug("load({})", identifier);
        return time("loadKey", identifier.owner(), () -> {
            final QueryRequest request = keyConverter.toActiveQueryRequest(identifier);
            final QueryResponse response = dynamoDbClientAccessor.query(request);
            LOGGER.debug("load:{}", response.consumedCapacity());
            if (response.hasItems() && response.items().size() > 0) {
                counterKey.increment(1);
                return Optional.of(keyConverter.from(response.items().get(0))); // first on the list is newest.
            } else {
                counterKey.increment(0);
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<OwnerIdentifier> loadOwner(final String ownerName) {
        LOGGER.debug("loadOwner({})", ownerName);
        return time("loadOwner", ownerName, () -> {
            final GetItemRequest request = ownerConverter.toOwnerGetItemRequest(ImmutableOwnerIdentifier.builder()
                    .owner(ownerName).build());
            final GetItemResponse response = dynamoDbClientAccessor.getItem(request);
            LOGGER.debug("loadOwner:{}", response.consumedCapacity());
            if (response.hasItem()) {
                counterOwner.increment(1);
                return Optional.of(ownerConverter.toOwnerIdentifier(response.item()));
            } else {
                counterOwner.increment(0);
                return Optional.empty();
            }
        });
    }

    @Override
    public Batch<OwnerIdentifier> listOwners(final Token nextToken) {
        LOGGER.debug("listOwners()");
        return time("listOwners", null, () -> {
            final QueryRequest request = ownerConverter.toOwnerSearchQueryRequest(nextToken);
            final QueryResponse response = dynamoDbClientAccessor.query(request);
            LOGGER.debug("listOwners:{}", response.consumedCapacity());
            return ownerConverter.toBatchOwnerIdentifier(response);
        });
    }


    @Override
    public Batch<KeyIdentifier> listKeys(final OwnerIdentifier identifier,
                                         final Token nextToken) {
        LOGGER.debug("listKeys({})", identifier);
        return time("listKeys", identifier.owner(), () -> {
            final QueryRequest request = ownerConverter.toOwnerQueryKeysRequest(identifier, nextToken);
            final QueryResponse response = dynamoDbClientAccessor.query(request);
            LOGGER.debug("listKeys:{}", response.consumedCapacity());
            return ownerConverter.toBatchKeyIdentifier(response);
        });
    }

    @Override
    public Batch<KeyVersionIdentifier> listVersions(final KeyIdentifier identifier,
                                                    final Token nextToken) {
        LOGGER.debug("listVersions({})", identifier);
        return time("listVersions", identifier.owner(), () -> {
            final QueryRequest request = keyConverter.toKeyVersionsQueryRequest(identifier, nextToken);
            final QueryResponse response = dynamoDbClientAccessor.query(request);
            LOGGER.debug("listVersions:{}", response.consumedCapacity());
            return keyConverter.toBatchKeyVersionIdentifier(response);
        });
    }

    @Override
    public boolean delete(final KeyVersionIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return time("deleteVersions", identifier.owner(), () -> {
            final DeleteItemRequest request = keyConverter.toDeleteRequest(identifier);
            final DeleteItemResponse response = dynamoDbClientAccessor.deleteItem(request);
            LOGGER.debug("deleteVersions:{}", response.consumedCapacity());
            return true;
        });
    }

    @Override
    public boolean delete(final KeyIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return time("deleteKey", identifier.owner(), () -> {
            return false;
        });
    }

    @Override
    public boolean delete(final OwnerIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return time("deleteOwner", identifier.owner(), () -> {
            return false;
        });
    }
}
