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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JsonManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonManager.class);

  private final ObjectMapper objectMapper;

  @Inject
  public JsonManager(final ObjectMapper objectMapper) {
    LOGGER.info("JsonManager({})", objectMapper);
    this.objectMapper = objectMapper;
  }

  /**
   * Wrapper so no one has to catch the JSON processing exception. Safe logging, only logs the class, not the json.
   *
   * @param json  to convert.
   * @param clazz to convert.
   * @param <T>   the type.
   * @return an instance of the type.
   */
  public <T> T readValue(final String json, final Class<T> clazz) {
    LOGGER.debug("readValue(json,{})", clazz);
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      LOGGER.error("Unable to read value for class: {}", clazz, e);
      throw new IllegalArgumentException("Unable to read value", e);
    }
  }

  public ObjectMapper objectMapper() {
    return objectMapper;
  }

  /**
   * Wrapper so no one has to catch the JSON processing exception. Safe logging, only logs the class, not the json.
   *
   * @param json          to convert.
   * @param typeReference to convert.
   * @param <T>           the type.
   * @return an instance of the type.
   */
  public <T> T readValue(final String json, final TypeReference<T> typeReference) {
    LOGGER.debug("readValue(json,{})", typeReference);
    try {
      return objectMapper.readValue(json, typeReference);
    } catch (JsonProcessingException e) {
      LOGGER.error("Unable to read value for class: {}", typeReference, e);
      throw new IllegalArgumentException("Unable to read value", e);
    }
  }

  public <T> String writeValue(final T object) {
    LOGGER.debug("writeValue(json,{})", object.getClass());
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      LOGGER.error("Unable to write value for class: {}", object.getClass(), e);
      throw new IllegalArgumentException("Unable to write value", e);
    }
  }

}
