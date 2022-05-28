// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.oop.OopMockConfiguration;
import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.manager.ResourceLookupManager;
import com.codeheadsystems.oop.mock.model.ImmutableInMemoryMockedDataStore;
import com.codeheadsystems.oop.mock.model.ImmutableMockedData;
import com.codeheadsystems.oop.mock.model.InMemoryMockedDataStore;
import com.google.common.collect.ImmutableMap;
import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InMemoryResolverTest {

    private static final String FILENAME = "filename";
    public static final String NAMESPACE = "namespace";
    public static final String LOOKUP = "lookup";
    public static final String DISCRIMINATOR = "discriminator";
    public static final String MARSHALLED_DATA = "marshaled";
    private static final Hasher HASHER = new Hasher("blah");
    private static final InMemoryMockedDataStore datastore = ImmutableInMemoryMockedDataStore.builder()
            .putDatastore(NAMESPACE, ImmutableMap.of(HASHER.hash(LOOKUP, DISCRIMINATOR),
                    ImmutableMockedData.builder().marshalledData(MARSHALLED_DATA).build()))
            .build();

    @Mock private OopMockConfiguration configuration;
    @Mock private JsonConverter converter;
    @Mock private ResourceLookupManager manager;
    @Mock private InputStream inputStream;

    private InMemoryResolver resolver;

    @BeforeEach
    void setUp() {
        when(configuration.mockDataFileName()).thenReturn(Optional.of(FILENAME));
        when(manager.inputStream(FILENAME)).thenReturn(Optional.of(inputStream));
        when(converter.convert(inputStream, InMemoryMockedDataStore.class)).thenReturn(datastore);
        resolver = new InMemoryResolver(configuration, converter, manager, HASHER);
    }

    @Test
    void resolve_nonamespace() {
        assertThat(resolver.resolve(NAMESPACE + "not here", LOOKUP, DISCRIMINATOR))
                .isEmpty();
    }

    @Test
    void resolve_nodiscriminator() {
        assertThat(resolver.resolve(NAMESPACE, LOOKUP, DISCRIMINATOR + "not here"))
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