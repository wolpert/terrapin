// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonConverterTest {

    private static final String JSON = "this is json";
    private static final Object RESULT = new Object();

    @Mock private InputStream inputStream;
    @Mock private ObjectMapper mapper;

    private JsonConverter converter;

    @BeforeEach
    void setup() {
        converter = new JsonConverter(mapper);
    }

    @Test
    void toJson_success() throws JsonProcessingException {
        when(mapper.writeValueAsString(RESULT))
                .thenReturn(JSON);

        assertThat(converter.toJson(RESULT))
                .isNotNull()
                .isEqualTo(JSON);
    }


    @Test
    void toJson_fail() throws JsonProcessingException {
        when(mapper.writeValueAsString(RESULT))
                .thenThrow(new OurException());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> converter.toJson(RESULT));
    }

    @Test
    void convert_string_success() throws JsonProcessingException {
        when(mapper.readValue(JSON, Object.class)).thenReturn(RESULT);

        assertThat(converter.convert(JSON, Object.class))
                .isNotNull()
                .isEqualTo(RESULT);
    }

    @Test
    void convert_string_ioexception() throws JsonProcessingException {
        when(mapper.readValue(JSON, Object.class)).thenThrow(new OurException());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> converter.convert(JSON, Object.class));
    }

    @Test
    void convert_inputstream_success() throws IOException {
        when(mapper.readValue(inputStream, Object.class)).thenReturn(RESULT);

        assertThat(converter.convert(inputStream, Object.class))
                .isNotNull()
                .isEqualTo(RESULT);
    }

    @Test
    void convert_inputstream_ioexception() throws IOException {
        when(mapper.readValue(inputStream, Object.class)).thenThrow(new IOException());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> converter.convert(inputStream, Object.class));
    }

    class OurException extends JsonProcessingException {

        protected OurException() {
            super("boom");
        }
    }

}