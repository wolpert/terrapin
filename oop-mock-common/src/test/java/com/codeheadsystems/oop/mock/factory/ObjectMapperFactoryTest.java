// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObjectMapperFactoryTest {

    private ObjectMapperFactory objectMapperFactory;

    @BeforeEach
    public void setup() {
        objectMapperFactory = new ObjectMapperFactory();
    }

    @Test
    void objectMapper() {

        final ObjectMapper mapper = objectMapperFactory.objectMapper();

        assertThat(mapper).isNotNull();
        // TODO: Figure out JDK8 is registered
    }
}