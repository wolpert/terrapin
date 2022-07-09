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

package com.codeheadsystems.oop.client;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.oop.client.dao.MockDataDAO;
import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.model.MockedData;
import com.codeheadsystems.oop.mock.translator.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OopMockClientTest {

    private static final String NAMESPACE = "namespace";
    private static final String LOOKUP = "lookup";
    private static final String ID = "id";
    private static final Class<?> CLAZZ = Object.class;
    private static final Object DATA = new Object();

    @Mock private MockedData mockedData;
    @Mock private Hasher hasher;
    @Mock private Translator translator;
    @Mock private MockDataDAO dao;

    private OopMockClient client;

    @BeforeEach
    public void setup() {
        when(hasher.namespace(CLAZZ)).thenReturn(NAMESPACE);
        client = new OopMockClient(CLAZZ, hasher, dao, translator);
    }

    @Test
    public void mockSetup() {
        when(translator.marshal(DATA)).thenReturn(mockedData);

        client.mockSetup(DATA, LOOKUP, ID);

        verify(dao).store(NAMESPACE, LOOKUP, ID, mockedData);
    }

    @Test
    public void deleteMock() {
        client.deleteMock(LOOKUP, ID);

        verify(dao).delete(NAMESPACE, LOOKUP, ID);
    }

}