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

import com.codeheadsystems.terrapin.server.dao.accessor.DynamoDbClientAccessor;
import com.codeheadsystems.terrapin.server.dao.converter.KeyConverter;
import com.codeheadsystems.terrapin.server.dao.model.Batch;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.OwnerIdentifier;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.ConsumedCapacity;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

@Singleton
public class KeyDAODynamoDB implements KeyDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyDAODynamoDB.class);

    private final TableConfiguration tableConfiguration;
    private final DynamoDbClientAccessor dynamoDbClientAccessor;
    private final KeyConverter keyConverter;

    @Inject
    public KeyDAODynamoDB(final DynamoDbClientAccessor dynamoDbClientAccessor,
                          final TableConfiguration tableConfiguration,
                          final KeyConverter keyConverter) {
        LOGGER.info("KeyDAODynamoDB({},{},{})", dynamoDbClientAccessor, tableConfiguration, keyConverter);
        this.tableConfiguration = tableConfiguration;
        this.dynamoDbClientAccessor = dynamoDbClientAccessor;
        this.keyConverter = keyConverter;
    }

    @Override
    public void store(final Key key) {
        LOGGER.debug("store({})", key.keyIdentifier());
        final PutItemRequest request = keyConverter.toPutItemRequest(key);
        final PutItemResponse response = dynamoDbClientAccessor.putItem(request);
        final ConsumedCapacity consumedCapacity = response.consumedCapacity();
        LOGGER.debug("store:{}", consumedCapacity);
    }

    @Override
    public Optional<Key> load(final KeyVersionIdentifier identifier) {
        LOGGER.debug("load({})", identifier);
        final GetItemRequest request = keyConverter.toGetItemRequest(identifier);
        final GetItemResponse response = dynamoDbClientAccessor.getItem(request);
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
}
