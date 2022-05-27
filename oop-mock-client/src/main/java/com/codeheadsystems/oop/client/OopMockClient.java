// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.client;

import com.codeheadsystems.oop.client.dao.MockDataDAO;
import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.model.MockedData;
import com.codeheadsystems.oop.mock.translator.Translator;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OopMockClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OopMockClient.class);

    private final MockDataDAO dao;
    private final Translator translator;
    private final String namespace;

    @AssistedInject
    public OopMockClient(@Assisted final Class<?> clazz,
                         final Hasher hasher,
                         final MockDataDAO dao,
                         final Translator translator) {
        LOGGER.info("OopMockClient({})", clazz);
        this.dao = dao;
        this.namespace = hasher.namespace(clazz);
        this.translator = translator;
    }

    public <R> void mockSetup(final R mockData,
                              final String lookup,
                              final String id) {
        final MockedData storedMockData = translator.marshal(mockData);
        dao.store(namespace, lookup, id, storedMockData);
    }

    public void deleteMock(final String lookup,
                           final String id) {
        dao.delete(namespace, lookup, id);
    }

}
