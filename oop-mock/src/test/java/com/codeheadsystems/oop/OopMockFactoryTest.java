// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.oop.dagger.ClassOopMockFactory;
import com.codeheadsystems.oop.mock.ClassOopMock;
import com.codeheadsystems.oop.mock.PassThroughOopMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OopMockFactoryTest {

    @Mock private OopMockConfiguration oopMockConfiguration;
    @Mock private ClassOopMockFactory classOopMockFactory;
    @Mock private PassThroughOopMock passThroughOopMoc;
    @Mock private ClassOopMock oopMock;

    private OopMockFactory oopMockFactory;


    @Test
    void generate_disabled() {
        when(oopMockConfiguration.enabled()).thenReturn(false);
        oopMockFactory = new OopMockFactory(oopMockConfiguration, classOopMockFactory, passThroughOopMoc);

        assertThat(oopMockFactory.generate(Object.class))
                .isEqualTo(passThroughOopMoc);
    }

    @Test
    void generate_enabled() {
        when(oopMockConfiguration.enabled()).thenReturn(true);
        when(classOopMockFactory.create(Object.class)).thenReturn(oopMock);
        oopMockFactory = new OopMockFactory(oopMockConfiguration, classOopMockFactory, passThroughOopMoc);

        assertThat(oopMockFactory.generate(Object.class))
                .isEqualTo(oopMock);
    }
}