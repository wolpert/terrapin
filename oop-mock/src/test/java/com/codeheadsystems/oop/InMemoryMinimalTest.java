// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.oop.dagger.DaggerMinimalSetupModule_FactoryBuilder;
import com.codeheadsystems.oop.dagger.InMemoryModule;
import com.codeheadsystems.oop.mock.manager.TimeManager;
import com.codeheadsystems.oop.mock.model.ImmutableInMemoryMockedDataStore;
import com.codeheadsystems.oop.mock.model.ImmutableMockedData;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

/**
 * Functional test though everything is inMemory.
 */
public class InMemoryMinimalTest {

    public static final String METHOD_MOCK_NAME = "ATest";
    public static final String ID = "ID";
    public static final String UNSET_ID = "UNSET";
    public static final String NAMESPACE = "DEFAULT:com.codeheadsystems.oop.InMemoryMinimalTest.ThrowException";
    public static final ImmutableMockedData MOCKED_TRUE = ImmutableMockedData.builder().marshalledData("true").build();
    public static final ImmutableInMemoryMockedDataStore DATA_STORE = ImmutableInMemoryMockedDataStore.builder()
            .putDatastore(NAMESPACE, ImmutableMap.of(METHOD_MOCK_NAME + "." + ID, MOCKED_TRUE))
            .build();

    /**
     * Given an explicit enabled configuration,
     * - mocked request with mock data set returns mocked data
     * - mocked request with no mock data set gets configuration exception.
     */
    @Test
    public void mockProxy_enabled() {
        final OopMockConfiguration config = ImmutableOopMockConfiguration.builder()
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
                .build();
        final ThrowException throwException = getTestWith(config);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(throwException::callMocked);
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(throwException::callNotMocked);
    }

    private ThrowException getTestWith(final OopMockConfiguration config) {
        return new ThrowException(DaggerMinimalSetupModule_FactoryBuilder.builder()
                .inMemoryModule(new InMemoryModule(DATA_STORE, config))
                .build().create());
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
            return timeManager.logTimed(() -> oopMock.proxy(Boolean.class, this::doIt, METHOD_MOCK_NAME,  UNSET_ID));
        }

    }

}
