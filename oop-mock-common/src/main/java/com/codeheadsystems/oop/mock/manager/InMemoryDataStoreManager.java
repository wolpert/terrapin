// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.manager;

import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.model.InMemoryMockedDataStore;
import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InMemoryDataStoreManager {

    private final JsonConverter converter;
    private final ResourceLookupManager manager;

    @Inject
    public InMemoryDataStoreManager(final JsonConverter converter,
                                    final ResourceLookupManager manager) {
        this.converter = converter;
        this.manager = manager;
    }

    public InMemoryMockedDataStore from(final String filename) {
        return manager.inputStream(filename).map(this::from)
                .orElseThrow(() -> new IllegalArgumentException("No such file for data store:" + filename));
    }

    public InMemoryMockedDataStore from(final InputStream inputStream) {
        return converter.convert(inputStream, InMemoryMockedDataStore.class);
    }
}
