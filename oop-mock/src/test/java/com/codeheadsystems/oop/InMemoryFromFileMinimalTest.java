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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.oop.dagger.DaggerOopMockFactoryBuilder;
import com.codeheadsystems.oop.mock.dagger.OopConfigurationModule;
import com.codeheadsystems.oop.mock.manager.TimeManager;
import org.junit.jupiter.api.Test;

/**
 * Functional test though everything is inMemory.
 */
public class InMemoryFromFileMinimalTest {

    public static final String METHOD_MOCK_NAME = "ATest";
    public static final String ID = "id";
    public static final String UNSET_ID = "UNSET";
    private static final String FILENAME = "testDataStore.json";

    /**
     * Given an explicit enabled configuration,
     * - mocked request with mock data set returns mocked data
     * - mocked request with no mock data set gets configuration exception.
     */
    @Test
    public void mockProxy_enabled() {
        final OopMockConfiguration config = ImmutableOopMockConfiguration.builder()
                .mockDataFileName(FILENAME)
                .enabled(true)
                .build();
        final ThrowException throwException = getTestWith(config);

        assertThat(throwException.callMocked())
                .isTrue();
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(throwException::callNotMocked);
    }

    /**
     * Given an explicit DISABLED configuration,
     * - mocked request with mock data set gets configuration exception.
     * - mocked request with no mock data set gets configuration exception.
     */
    @Test
    public void mockProxy_notEnabled_explicit() {
        final OopMockConfiguration config = ImmutableOopMockConfiguration.builder()
                .mockDataFileName(FILENAME)
                .enabled(false)
                .build();
        final ThrowException throwException = getTestWith(config);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(throwException::callMocked);
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(throwException::callNotMocked);
    }

    /**
     * Given an IMPLICIT DISABLED configuration (no configuration set),
     * - mocked request with mock data set gets configuration exception.
     * - mocked request with no mock data set gets configuration exception.
     */
    @Test
    public void mockProxy_notEnabled_implicit() {
        final OopMockConfiguration config = ImmutableOopMockConfiguration.builder()
                .mockDataFileName(FILENAME)
                .build();
        final ThrowException throwException = getTestWith(config);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(throwException::callMocked);
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(throwException::callNotMocked);
    }

    private ThrowException getTestWith(final OopMockConfiguration config) {
        return new ThrowException(DaggerOopMockFactoryBuilder.builder()
                .oopConfigurationModule(new OopConfigurationModule(config))
                .build()
                .factory());
    }

    class ThrowException {

        private final TimeManager timeManager;

        private final OopMock oopMock;

        ThrowException(final OopMockFactory factory) {
            oopMock = factory.generate(getClass());
            timeManager = new TimeManager();
        }

        public Boolean callMocked() {
            return timeManager.logTimed(() -> oopMock.proxy(Boolean.class, this::doIt, METHOD_MOCK_NAME, ID));
        }

        public boolean doIt() {
            throw new IllegalStateException("boom");
        }

        public Boolean callNotMocked() {
            return timeManager.logTimed(() -> oopMock.proxy(Boolean.class, this::doIt, METHOD_MOCK_NAME, UNSET_ID));
        }

    }

}
