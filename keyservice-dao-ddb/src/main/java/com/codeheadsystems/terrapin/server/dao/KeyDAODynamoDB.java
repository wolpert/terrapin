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
import com.codeheadsystems.terrapin.server.dao.model.Batch;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.OwnerIdentifier;
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
    private final TableConfiguration tableConfiguration;
    private final DynamoDbClientAccessor dynamoDbClientAccessor;
    private final KeyConverter keyConverter;
    private final Metrics metrics;

    @Inject
    public KeyDAODynamoDB(final DynamoDbClientAccessor dynamoDbClientAccessor,
                          final TableConfiguration tableConfiguration,
                          final KeyConverter keyConverter,
                          final Metrics metrics) {
        LOGGER.info("KeyDAODynamoDB({},{},{})", dynamoDbClientAccessor, tableConfiguration, keyConverter);
        this.tableConfiguration = tableConfiguration;
        this.dynamoDbClientAccessor = dynamoDbClientAccessor;
        this.keyConverter = keyConverter;
        this.metrics = metrics;
    }

    private <T> T time(final String methodName,
                       final String owner,
                       final Supplier<T> supplier) {
        final String name = "ddbdao." + methodName;
        final Timer timer;
        if (owner != null) {
            timer = metrics.registry().timer(name, OWNER, owner); // TODO: Vet cardinality. Set by configuration?
        } else {
            timer = metrics.registry().timer(name);
        }
        return metrics.time(name, timer, supplier);
    }

    @Override
    public void store(final Key key) {
        LOGGER.debug("store({})", key.keyVersionIdentifier());
        time("store", key.keyVersionIdentifier().owner(), () -> {
            final PutItemRequest request = keyConverter.toPutItemRequest(key);
            final PutItemResponse response = dynamoDbClientAccessor.putItem(request);
            final ConsumedCapacity consumedCapacity = response.consumedCapacity();
            LOGGER.debug("store:{}", consumedCapacity);
            return null;
        });
    }

    @Override
    public Optional<Key> load(final KeyVersionIdentifier identifier) {
        LOGGER.debug("load({})", identifier);
        return time("loadversion", identifier.owner(), () -> {
            final GetItemRequest request = keyConverter.toGetItemRequest(identifier);
            final GetItemResponse response = dynamoDbClientAccessor.getItem(request);
            final ConsumedCapacity consumedCapacity = response.consumedCapacity();
            LOGGER.debug("load:{}", consumedCapacity);
            if (response.hasItem()) {
                return Optional.of(keyConverter.from(response));
            } else {
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
        return time("loadkey", identifier.owner(), () -> {
            final QueryRequest request = keyConverter.toActiveQueryRequest(identifier);
            final QueryResponse response = dynamoDbClientAccessor.query(request);
            LOGGER.debug("load:{}", response.consumedCapacity());
            if (response.hasItems() && response.items().size() > 0) {
                return Optional.of(keyConverter.from(response.items().get(0))); // first on the list is newest.
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<OwnerIdentifier> loadOwner(final String ownerName) {
        LOGGER.debug("loadOwner({})", ownerName);
        return time("loadowner", ownerName, () -> {
            return Optional.empty();
        });
    }

    @Override
    public Batch<OwnerIdentifier> listOwners() {
        LOGGER.debug("listOwners()");
        return time("listowners", null, () -> {
            return null;
        });
    }


    @Override
    public Batch<KeyIdentifier> listKeys(final OwnerIdentifier identifier) {
        LOGGER.debug("listKeys({})", identifier);
        return time("listkeys", identifier.owner(), () -> {
            return null;
        });
    }

    @Override
    public Batch<KeyVersionIdentifier> listVersions(final KeyIdentifier identifier) {
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
