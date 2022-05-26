// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.codeheadsystems.oop.mock.model.MockedData;
import com.codeheadsystems.oop.mock.resolver.MockDataResolver;
import com.codeheadsystems.oop.mock.translator.Translator;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProxyManagerTest {

    private static final Object REAL_RESULT = new Object();
    private static final Object MOCK_RESULT = new Object();
    private static final String NAMESPACE = "namespace";
    private static final String LOOKUP = "lookup";
    private static final String ID = "ID";

    @Mock private MockDataResolver resolver;
    @Mock private Translator translator;
    @Mock private Supplier<Object> supplier;
    @Mock private MockedData mockedData;

    private ProxyManager manager;

    @BeforeEach
    public void setup() {
        manager = new ProxyManager(resolver, translator);
    }

    @Test
    public void proxy_dataFound(){
        when(resolver.resolve(NAMESPACE,LOOKUP,ID)).thenReturn(Optional.of(mockedData));
        when(translator.unmarshal(Object.class, mockedData)).thenReturn(MOCK_RESULT);

        assertThat(manager.proxy(NAMESPACE, LOOKUP, ID, Object.class, supplier))
                .isNotNull()
                .isEqualTo(MOCK_RESULT);
    }


    @Test
    public void proxy_dataNotFound(){
        when(resolver.resolve(NAMESPACE,LOOKUP,ID)).thenReturn(Optional.empty());
        when(supplier.get()).thenReturn(REAL_RESULT);

        assertThat(manager.proxy(NAMESPACE, LOOKUP, ID, Object.class, supplier))
                .isNotNull()
                .isEqualTo(REAL_RESULT);
    }


}