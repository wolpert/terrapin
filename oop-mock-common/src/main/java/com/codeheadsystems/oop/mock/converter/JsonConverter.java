// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

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
