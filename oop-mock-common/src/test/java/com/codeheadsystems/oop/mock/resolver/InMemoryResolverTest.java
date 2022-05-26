// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.model.ImmutableInMemoryMockedDataStore;
import com.codeheadsystems.oop.mock.model.ImmutableMockedData;
import com.codeheadsystems.oop.mock.model.InMemoryMockedDataStore;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryResolverTest {

    public static final String NAMESPACE = "namespace";
    public static final String LOOKUP = "lookup";
    public static final String DISCRIMINATOR = "discriminator";
    public static final String MARSHALLED_DATA = "marshaled";
    private static final Hasher HASHER = new Hasher("blah");
    private static final InMemoryMockedDataStore datastore = ImmutableInMemoryMockedDataStore.builder()
            .putDatastore(NAMESPACE, ImmutableMap.of(HASHER.hash(LOOKUP, DISCRIMINATOR),
                    ImmutableMockedData.builder().marshalledData(MARSHALLED_DATA).build()))
            .build();


    private InMemoryResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new InMemoryResolver(datastore, HASHER);
    }

    @Test
    void resolve_nonamespace() {
        assertThat(resolver.resolve(NAMESPACE + "not here",LOOKUP, DISCRIMINATOR))
                .isEmpty();
    }

    @Test
    void resolve_nodiscriminator() {
        assertThat(resolver.resolve(NAMESPACE, LOOKUP,DISCRIMINATOR+ "not here"))
                .isEmpty();
    }

    @Test
    void resolve() {
        assertThat(resolver.resolve(NAMESPACE, LOOKUP, DISCRIMINATOR))
                .isNotEmpty()
                .get()
                .hasFieldOrPropertyWithValue("marshalledData", MARSHALLED_DATA);
    }
}