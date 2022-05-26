// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HasherTest {

    private static final String SYSTEM = "DEFAULT";
    
    @Test
    void hash() {
        final Hasher hasher = new Hasher(SYSTEM);
        final String result = hasher.hash("a","b","c");

        assertThat(result)
                .isEqualTo("a.b.c");
    }

    @Test
    void namespace_empty() {
        final Hasher hasher = new Hasher(SYSTEM);
        final String result = hasher.namespace(HasherTest.class);

        assertThat(result)
                .isEqualTo("DEFAULT:com.codeheadsystems.oop.mock.HasherTest");
    }

    @Test
    void namespace_notEmpty() {
        final String app = "OppMock";
        final Hasher hasher = new Hasher(app);
        final String result = hasher.namespace(HasherTest.class);

        assertThat(result)
                .isEqualTo(app + ":com.codeheadsystems.oop.mock.HasherTest");
    }
}