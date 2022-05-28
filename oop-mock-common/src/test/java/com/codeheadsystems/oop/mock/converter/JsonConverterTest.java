/*
 *    Copyright (c) 2022 Ned Wolpert <ned.wolpert@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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