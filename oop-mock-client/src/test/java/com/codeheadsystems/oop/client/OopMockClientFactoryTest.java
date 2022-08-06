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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.oop.client.dagger.OopMockClientAssistedFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OopMockClientFactoryTest {

  @Mock private OopMockClientAssistedFactory assistedFactory;
  @Mock private OopMockClient client;

  private OopMockClientFactory factory;

  @BeforeEach
  void setup() {
    factory = new OopMockClientFactory(assistedFactory);
  }

  @Test
  void generate() {
    when(assistedFactory.create(Object.class)).thenReturn(client);

    final OopMockClient client1 = factory.generate(Object.class);
    final OopMockClient client2 = factory.generate(Object.class);

    assertThat(client1)
        .isNotNull()
        .isEqualTo(client)
        .isEqualTo(client2);
  }
}