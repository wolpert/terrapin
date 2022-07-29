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

package com.codeheadsystems.terrapin.common.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonManagerTest {

  private static final String JSON = "{this is json, really}";
  private static final Object FROM_JSON = new Object();

  @Mock private ObjectMapper mapper;
  @Mock private TypeReference<Object> typeReference;

  private JsonManager manager;

  @BeforeEach
  void setUp() {
    manager = new JsonManager(mapper);
  }

  @Test
  public void testObjectMapper() {
    final ObjectMapper result = manager.objectMapper();
    assertThat(result)
            .isEqualTo(mapper);
  }

  @Test
  void readValue_success() throws JsonProcessingException {
    when(mapper.readValue(JSON, Object.class)).thenReturn(FROM_JSON);

    final Object result = manager.readValue(JSON, Object.class);

    assertThat(result)
            .isNotNull()
            .isEqualTo(FROM_JSON);
  }

  @Test
  void readValue_success_typeref() throws JsonProcessingException {
    when(mapper.readValue(JSON, typeReference)).thenReturn(FROM_JSON);

    final Object result = manager.readValue(JSON, typeReference);

    assertThat(result)
            .isNotNull()
            .isEqualTo(FROM_JSON);
  }

  @Test
  void readValue_exception() throws JsonProcessingException {
    when(mapper.readValue(JSON, Object.class)).thenThrow(new FakeException("this is a test"));

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> manager.readValue(JSON, Object.class))
        .withMessageContaining("Unable to read value");
  }

  @Test
  void readValue_exception_typeRef() throws JsonProcessingException {
    when(mapper.readValue(JSON, typeReference)).thenThrow(new FakeException("this is a test"));

    assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> manager.readValue(JSON, typeReference))
            .withMessageContaining("Unable to read value");
  }

  @Test
  void writeValue() throws JsonProcessingException {
    when(mapper.writeValueAsString(FROM_JSON)).thenReturn(JSON);

    assertThat(manager.writeValue(FROM_JSON))
            .isNotNull()
            .isEqualTo(JSON);
  }

  @Test
  void writeValue_exception() throws JsonProcessingException {
    when(mapper.writeValueAsString(FROM_JSON)).thenThrow(new FakeException("boom"));

    assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> manager.writeValue(FROM_JSON))
            .withMessageContaining("Unable to write value");
  }

  class FakeException extends JsonProcessingException {

    protected FakeException(String msg) {
      super(msg);
    }
  }

}