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
import software.amazon.awssdk.services.dynamodb.model.ConsumedCapacity;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

@Singleton
public class KeyDAODynamoDB implements KeyDAO {
    public static final String OWNER = "owner";
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyDAODynamoDB.class);
    public static final String PREFIX = "ddbdao.";
    private final DynamoDbClientAccessor dynamoDbClientAccessor;
    private final KeyConverter keyConverter;
    private final OwnerConverter ownerConverter;
    private final Metrics metrics;
    private final Counter counterKeyVersion;
    private final Counter counterKey;
    private final Counter counterOwner;

    @Inject
    public KeyDAODynamoDB(final DynamoDbClientAccessor dynamoDbClientAccessor,
                          final KeyConverter keyConverter,
                          final OwnerConverter ownerConverter,
                          final Metrics metrics) {
        LOGGER.info("KeyDAODynamoDB({},{},{})", dynamoDbClientAccessor, keyConverter, ownerConverter);
        this.dynamoDbClientAccessor = dynamoDbClientAccessor;
        this.keyConverter = keyConverter;
        this.ownerConverter = ownerConverter;
        this.metrics = metrics;
        final MeterRegistry registry = metrics.registry();
        counterKeyVersion = registry.counter(PREFIX + "found.key.version");
        counterKey = registry.counter(PREFIX + "found.key");
        counterOwner = registry.counter(PREFIX + "found.owner");
    }

    private <T> T time(final String methodName,
                       final String owner,
                       final Supplier<T> supplier) {
        final String name = PREFIX + methodName;
        final Timer timer;
        if (owner != null) {
            timer = metrics.registry().timer(name, OWNER, owner); // TODO: Vet cardinality. Set by configuration?
        } else {
            timer = metrics.registry().timer(name, OWNER, "null");
        }
        return metrics.time(name, timer, supplier);
    }

    @Override
    public void store(final Key key) {
        LOGGER.debug("store({})", key.keyVersionIdentifier());
        time("storeKey", key.keyVersionIdentifier().owner(), () -> {
            storeKey(key);
            storeOwner(key);
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
            return ownerConverter.toBatchKeyIdentifier(response);
        });
    }

    @Override
    public Batch<KeyVersionIdentifier> listVersions(final KeyIdentifier identifier,
                                                    final Token nextToken) {
        LOGGER.debug("listVersions({})", identifier);
        return time("listversions", identifier.owner(), () -> {
            return null;
        });
    }

    @Override
    public boolean delete(final KeyVersionIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return time("deleteversions", identifier.owner(), () -> {
            return false;
        });
    }

    @Override
    public boolean delete(final KeyIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return time("deletekey", identifier.owner(), () -> {
            return false;
        });
    }

    @Override
    public boolean delete(final OwnerIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return time("deleteowner", identifier.owner(), () -> {
            return false;
        });
    }
}
