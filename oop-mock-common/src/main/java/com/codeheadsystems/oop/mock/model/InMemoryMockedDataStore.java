// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableInMemoryMockedDataStore.class)
@JsonDeserialize(builder = ImmutableInMemoryMockedDataStore.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface InMemoryMockedDataStore {

    /**
     * Provides an inmemory datastore of all mocked data. When in use, this should  be loaded up on
     * boot and used. Note that this is mostly used for testing or writing mocked data. Unless your
     * needs are small, you likely want a real datastore.
     *
     * @return Namespace->discriminator->mocked data map. (discriminator = lookup.id as processed by hasher)
     */
    Map<String, Map<String, MockedData>> datastore();

}
