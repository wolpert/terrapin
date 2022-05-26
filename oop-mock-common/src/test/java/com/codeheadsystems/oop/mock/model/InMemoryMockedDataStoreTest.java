// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.model;

import com.codeheadsystems.test.model.BaseJacksonTest;
import com.google.common.collect.ImmutableMap;

class InMemoryMockedDataStoreTest extends BaseJacksonTest<InMemoryMockedDataStore> {

    @Override
    protected Class<InMemoryMockedDataStore> getBaseClass() {
        return InMemoryMockedDataStore.class;
    }

    @Override
    protected InMemoryMockedDataStore getInstance() {
        return ImmutableInMemoryMockedDataStore.builder()
                .putDatastore("key",
                        ImmutableMap.of("otherkey",
                                ImmutableMockedData.builder().marshalledData("marshalled").build()))
                .build();
    }
}