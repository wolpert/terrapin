// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.codeheadsystems.test.model.BaseJacksonTest;
import org.junit.jupiter.api.Test;

class OopMockConfigurationTest extends BaseJacksonTest<OopMockConfiguration> {

    public static final String MOCK_DATA_FILE_NAME = "filename";

    @Override
    protected Class<OopMockConfiguration> getBaseClass() {
        return OopMockConfiguration.class;
    }

    @Override
    protected OopMockConfiguration getInstance() {
        return ImmutableOopMockConfiguration.builder()
                .mockDataFileName(MOCK_DATA_FILE_NAME)
                .delayResponseEnabled(true)
                .enabled(true)
                .resolverConfiguration(ImmutableResolverConfiguration.builder()
                        .addConfigurationLines("config 1")
                        .resolverClass("clazz")
                        .build())
                .build();
    }

    @Test
    void testMockDataFileNamed() {
        assertThat(getInstance().mockDataFileName())
                .isNotEmpty()
                .get().isEqualTo(MOCK_DATA_FILE_NAME);
        assertThat(ImmutableOopMockConfiguration.builder().build().mockDataFileName())
                .isEmpty();
    }

    @Test
    void testEnabled() {
        assertThat(getInstance().enabled())
                .isTrue();
        assertThat(ImmutableOopMockConfiguration.builder().build().enabled())
                .isFalse();
    }

    @Test
    void testDelayResponseEnabled() {
        assertThat(getInstance().delayResponseEnabled())
                .isTrue();
        assertThat(ImmutableOopMockConfiguration.builder().build().delayResponseEnabled())
                .isFalse();
    }
}