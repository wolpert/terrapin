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
                .maxDelayTimeMS(10000L)
                .namespace("namespace")
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