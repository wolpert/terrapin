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