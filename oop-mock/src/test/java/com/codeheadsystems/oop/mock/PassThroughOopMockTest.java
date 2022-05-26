// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassThroughOopMockTest {

    private PassThroughOopMock passThroughOopMock;

    @BeforeEach
    public void setup() {
        passThroughOopMock = new PassThroughOopMock();
    }

    @Test
    void proxy() {
        final String str = "not a test";
        final String result = passThroughOopMock.proxy(String.class, () -> str, "a", "b");

        assertThat(result)
                .isEqualTo(str);
    }

    @Test
    void testToString() {
        assertThat(passThroughOopMock.toString())
                .isNotNull()
                .contains("disabled");
    }
}