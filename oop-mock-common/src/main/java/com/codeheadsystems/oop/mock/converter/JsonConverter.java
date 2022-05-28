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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JsonConverter {

    private final ObjectMapper mapper;

    @Inject
    public JsonConverter(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String toJson(final Object resource) {
        try {
            return mapper.writeValueAsString(resource);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to convert resource type:" + resource.getClass(), e);
        }
    }

    public <R> R convert(final String json, final Class<R> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to convert json string to " + clazz, e);
        }
    }

    public <R> R convert(final InputStream inputStream, final Class<R> clazz) {
        try {
            return mapper.readValue(inputStream, clazz); // this will close the stream automatically.
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to convert input stream to " + clazz, e);
        }
    }

}
