// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dagger;

import com.codeheadsystems.oop.OopMockConfiguration;
import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.model.InMemoryMockedDataStore;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public class InMemoryModule {

    private final InMemoryMockedDataStore dataStore;
    private final OopMockConfiguration configuration;

    public InMemoryModule(final InMemoryMockedDataStore dataStore,
                          final OopMockConfiguration configuration) {
        this.dataStore = dataStore;
        this.configuration = configuration;
    }

    @Provides
    @Singleton
    public OopMockConfiguration configuration() {
        return configuration;
    }

    @Provides
    @Singleton
    public InMemoryMockedDataStore inMemoryMockedDataStore(final JsonConverter jsonConverter) {
        System.out.println(jsonConverter.toJson(dataStore));
        return dataStore;
    }

}
