// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceLookupManagerTest {

    private ResourceLookupManager manager;

    @BeforeEach
    void setup() {
        manager = new ResourceLookupManager(Optional.empty());
    }

    @Test
    void inputStream() {
        assertThat(manager.inputStream("logback.xml"))
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    void inputStream_notFound() {
        assertThat(manager.inputStream("I do not ExIsT"))
                .isNotNull()
                .isEmpty();
    }
}