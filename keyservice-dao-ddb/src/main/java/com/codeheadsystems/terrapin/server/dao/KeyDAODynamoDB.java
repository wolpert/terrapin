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
import com.codeheadsystems.metrics.MetricsHelper;
import com.codeheadsystems.metrics.MetricsName;
import com.codeheadsystems.terrapin.server.dao.converter.KeyConverter;
import com.codeheadsystems.terrapin.server.dao.model.Batch;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.OwnerIdentifier;
import com.codeheadsystems.terrapin.server.exception.DependencyException;
import com.codeheadsystems.terrapin.server.exception.RetryableException;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Singleton
public class KeyDAODynamoDB implements KeyDAO {
    public static final String STORE_KEY_METRIC = MetricsName.name(KeyDAODynamoDB.class, "store", "key");
    public static final String LOAD_KEY_VERSION_METRIC = MetricsName.name(KeyDAODynamoDB.class, "load", "key", "version");

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyDAODynamoDB.class);

    private final TableConfiguration tableConfiguration;
    private final DynamoDbClient dynamoDbClient;
    private final KeyConverter keyConverter;
    private final MetricsHelper metricsHelper;

    @Inject
    public KeyDAODynamoDB(final DynamoDbClient dynamoDbClient,
                          final TableConfiguration tableConfiguration,
                          final KeyConverter keyConverter,
                          final MetricsHelper metricsHelper) {
        LOGGER.info("KeyDAODynamoDB({},{},{})", dynamoDbClient, tableConfiguration, keyConverter);
        this.tableConfiguration = tableConfiguration;
        this.dynamoDbClient = dynamoDbClient;
        this.keyConverter = keyConverter;
        this.metricsHelper = metricsHelper;
    }

    @Override
    public void store(final Key key) {
        LOGGER.debug("store({})", key.keyIdentifier());
        final Metrics metrics = metricsHelper.get();
        final PutItemRequest request = keyConverter.toPutItemRequest(key);
        final PutItemResponse response = exceptionCheck(STORE_KEY_METRIC, () -> dynamoDbClient.putItem(request));
        final ConsumedCapacity consumedCapacity = response.consumedCapacity();
        LOGGER.debug("store:{}", consumedCapacity);
    }

    @Override
    public Optional<Key> load(final KeyVersionIdentifier identifier) {
        LOGGER.debug("load({})", identifier);
        final Metrics metrics = metricsHelper.get();
        final GetItemRequest request = keyConverter.toGetItemRequest(identifier);
        final GetItemResponse response = exceptionCheck(LOAD_KEY_VERSION_METRIC, () -> dynamoDbClient.getItem(request));
        if (response.hasItem()) {
            return Optional.of(keyConverter.from(response));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Key> load(final KeyIdentifier identifier) {
        LOGGER.debug("load({})", identifier);
        return Optional.empty();
    }

    @Override
    public Optional<OwnerIdentifier> loadOwner(final String ownerName) {
        LOGGER.debug("loadOwner({})", ownerName);
        return Optional.empty();
    }

    @Override
    public Batch<OwnerIdentifier> listOwners() {
        LOGGER.debug("listOwners()");
        return null;
    }


    @Override
    public Batch<KeyIdentifier> listKeys(final OwnerIdentifier identifier) {
        LOGGER.debug("listKeys({})", identifier);
        return null;
    }

    @Override
    public Batch<KeyVersionIdentifier> listVersions(final KeyIdentifier identifier) {
        LOGGER.debug("listVersions({})", identifier);
        return null;
    }

    @Override
    public boolean delete(final KeyVersionIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return false;
    }

    @Override
    public boolean delete(final KeyIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return false;
    }

    @Override
    public boolean delete(final OwnerIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return false;
    }

    private <T> T exceptionCheck(final String metricName, Supplier<T> supplier) {
        try {
            return metricsHelper.time(metricName, supplier);
        } catch (ProvisionedThroughputExceededException | TransactionConflictException | RequestLimitExceededException |
                 InternalServerErrorException e) {
            throw new RetryableException(e);
        } catch (RuntimeException e) {
            throw new DependencyException(e);
        }
    }
}
