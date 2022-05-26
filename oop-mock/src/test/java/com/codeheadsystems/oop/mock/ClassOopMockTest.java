// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.oop.manager.ProxyManager;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassOopMockTest {

    private static final Class<?> CLAZZ = Object.class;
    private static final String LOOKUP = "lookup";
    private static final String ID = "B";
    private static final String NAMESPACE = "ANYTHING";
    private static final String REAL_RESPONSE = "real response";
    private static final String MOCK_RESPONSE = "I'm a mock!";

    @Mock private Hasher hasher;
    @Mock private ProxyManager proxyManager;

    private ClassOopMock classOopMock;

    @BeforeEach
    public void setup() {
        when(hasher.namespace(CLAZZ)).thenReturn(NAMESPACE);
        classOopMock = new ClassOopMock(CLAZZ, hasher, proxyManager);
    }

    @Test
    void proxy() {
        final Supplier<String> supplier = () -> REAL_RESPONSE;
        when(proxyManager.proxy(NAMESPACE, LOOKUP, ID, String.class, supplier)).thenReturn(MOCK_RESPONSE);

        final String result = classOopMock.proxy(String.class, supplier, LOOKUP, ID);

        assertThat(result)
                .isEqualTo(MOCK_RESPONSE);
    }

    @Test
    void testToString() {
        assertThat(classOopMock.toString())
                .isNotNull();
    }
}