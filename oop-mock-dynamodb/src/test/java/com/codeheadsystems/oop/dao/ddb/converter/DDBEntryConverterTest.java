// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dao.ddb.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.codeheadsystems.oop.dao.ddb.model.DDBEntry;
import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.model.MockedData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DDBEntryConverterTest {

    public static final String NAMESPACE = "namespace";
    public static final String LOOKUP = "lookup";
    public static final String DISCRIMINATOR = "discriminator";
    public static final String HASH = "hash";
    public static final String RANGE = "range";
    private static final String JSON = "JSON";
    public static final DDBEntry ENTRY_WITHOUT_DATA = new DDBEntry(HASH, RANGE);
    public static final DDBEntry ENTRY_WITH_DATA = new DDBEntry(HASH, RANGE, JSON);

    @Mock private Hasher hasher;
    @Mock private JsonConverter jsonConverter;
    @Mock private MockedData mockedData;

    private DDBEntryConverter converter;

    @BeforeEach
    void setup() {
        converter = new DDBEntryConverter(hasher, jsonConverter);
    }

    @Test
    void convert_withoutMockData() {
        when(hasher.hash(LOOKUP, DISCRIMINATOR)).thenReturn(RANGE);

        assertThat(converter.convert(NAMESPACE, LOOKUP, DISCRIMINATOR))
                .isNotNull()
                .hasFieldOrPropertyWithValue("hash", NAMESPACE)
                .hasFieldOrPropertyWithValue("range", RANGE)
                .hasFieldOrPropertyWithValue("mockData", null);
    }

    @Test
    void convert_withMockData() {
        when(hasher.hash(LOOKUP, DISCRIMINATOR)).thenReturn(RANGE);
        when(jsonConverter.toJson(mockedData)).thenReturn(JSON);

        assertThat(converter.convert(NAMESPACE, LOOKUP, DISCRIMINATOR, mockedData))
                .isNotNull()
                .hasFieldOrPropertyWithValue("hash", NAMESPACE)
                .hasFieldOrPropertyWithValue("range", RANGE)
                .hasFieldOrPropertyWithValue("mockData", JSON);
    }

    @Test
    void toMockedData_nofield() {
        assertThat(converter.toMockedData(ENTRY_WITHOUT_DATA))
                .isEmpty();
    }

    @Test
    void toMockedData_withfield() {
        when(jsonConverter.convert(JSON, MockedData.class)).thenReturn(mockedData);

        assertThat(converter.toMockedData(ENTRY_WITH_DATA))
                .isNotEmpty()
                .contains(mockedData);
    }
}