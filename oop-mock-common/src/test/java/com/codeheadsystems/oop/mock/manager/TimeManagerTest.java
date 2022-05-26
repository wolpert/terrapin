// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TimeManagerTest {

    private TimeManager manager;

    @BeforeEach
    public void setup() {
        manager = new TimeManager();
    }

    @Test
    void logTimed() {
        final Boolean result = manager.logTimed(() -> Boolean.TRUE);

        assertThat(result)
                .isNotNull()
                .isTrue();

    }
}