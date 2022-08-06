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
import static org.mockito.Mockito.when;

import com.codeheadsystems.oop.dagger.ClassOopMockFactory;
import com.codeheadsystems.oop.dagger.OopMockFactoryBuilder;
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

  @Test
  void generate_instance() {
    oopMockFactory = OopMockFactoryBuilder.generate();

    final OopMock mock = oopMockFactory.generate(Object.class);
    assertThat(mock)
        .isNotNull()
        .extracting(Object::getClass)
        .isNotEqualTo(PassThroughOopMock.class)
        .isEqualTo(ClassOopMock.class);
  }
}