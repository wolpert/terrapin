// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.model.MockedData;
import com.codeheadsystems.oop.mock.translator.JsonTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonTranslatorTest {

    private static final String MARSHALLED = "marshalled";
    private static final String REAL_DATA = "real data";

    @Mock private JsonConverter converter;
    @Mock private MockedData mockedData;

    private JsonTranslator translator;

    @BeforeEach
    void setUp() {
        translator = new JsonTranslator(converter);
    }

    @Test
    void unmarshal() {
        when(mockedData.marshalledData()).thenReturn(MARSHALLED);
        when(converter.convert(MARSHALLED, String.class)).thenReturn(REAL_DATA);

        final String result = translator.unmarshal(String.class, mockedData);

        assertThat(result)
                .isEqualTo(REAL_DATA);
    }

    @Test
    void marshal() {
        when(converter.toJson(REAL_DATA))
                .thenReturn(MARSHALLED);

        final MockedData result = translator.marshal(REAL_DATA);

        assertThat(result)
                .isNotNull()
                .hasFieldOrPropertyWithValue("marshalledData", MARSHALLED);

    }

}