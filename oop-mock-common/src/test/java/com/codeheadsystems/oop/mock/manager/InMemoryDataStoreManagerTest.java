// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.model.InMemoryMockedDataStore;
import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InMemoryDataStoreManagerTest {
    private static final String FILENAME = "filename";

    @Mock private InMemoryMockedDataStore dataStore;
    @Mock private JsonConverter converter;
    @Mock private ResourceLookupManager lookupManager;
    @Mock private InputStream inputStream;

    private InMemoryDataStoreManager manager;

    @BeforeEach
    void setup() {
        manager = new InMemoryDataStoreManager(converter, lookupManager);
    }

    @Test
    void testFromInputStream() {
        when(converter.convert(inputStream, InMemoryMockedDataStore.class))
                .thenReturn(dataStore);

        assertThat(manager.from(inputStream))
                .isNotNull()
                .isEqualTo(dataStore);
    }

    @Test
    void testFromFilename_found() {
        when(lookupManager.inputStream(FILENAME))
                .thenReturn(Optional.of(inputStream));
        when(converter.convert(inputStream, InMemoryMockedDataStore.class))
                .thenReturn(dataStore);

        assertThat(manager.from(FILENAME))
                .isNotNull()
                .isEqualTo(dataStore);
    }

    @Test
    void testFromFilename_notFound() {
        when(lookupManager.inputStream(FILENAME))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> manager.from(FILENAME));
    }


}