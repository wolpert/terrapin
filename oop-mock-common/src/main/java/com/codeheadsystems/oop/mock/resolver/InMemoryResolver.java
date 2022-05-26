// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.resolver;

import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.model.InMemoryMockedDataStore;
import com.codeheadsystems.oop.mock.model.MockedData;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InMemoryResolver implements MockDataResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryResolver.class);

    protected final InMemoryMockedDataStore dataStore;
    protected final Hasher hasher;

    @Inject
    public InMemoryResolver(final InMemoryMockedDataStore dataStore,
                            final Hasher hasher) {
        this.hasher = hasher;
        LOGGER.info("InMemoryResolver({})", dataStore.datastore().keySet());
        this.dataStore = dataStore;
    }

    @Override
    public Optional<MockedData> resolve(final String namespace,
                                        final String lookup,
                                        final String discriminator) {
        LOGGER.debug("resolve({},{},{})", namespace, lookup, discriminator);
        final Map<String, MockedData> discriminatorMap = dataStore.datastore().get(namespace);
        if (discriminatorMap == null) {
            LOGGER.debug("-> no namespace");
            return Optional.empty();
        } else {
            final String aggregator = hasher.hash(lookup, discriminator);
            final MockedData mockedData = discriminatorMap.get(aggregator);
            LOGGER.debug("-> discriminator found: {}", mockedData != null);
            return Optional.ofNullable(mockedData);
        }
    }
}
