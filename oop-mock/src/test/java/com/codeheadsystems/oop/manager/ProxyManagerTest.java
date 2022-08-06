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

package com.codeheadsystems.oop.manager;

import static org.assertj.core.api.Assertions.assertThat;
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
  @Mock private DelayManager delayManager;

  private ProxyManager manager;

  @BeforeEach
  public void setup() {
    manager = new ProxyManager(resolver, translator, delayManager);
  }

  @Test
  public void proxy_dataFound() {
    when(resolver.resolve(NAMESPACE, LOOKUP, ID)).thenReturn(Optional.of(mockedData));
    when(translator.unmarshal(Object.class, mockedData)).thenReturn(MOCK_RESULT);

    assertThat(manager.proxy(NAMESPACE, LOOKUP, ID, Object.class, supplier))
        .isNotNull()
        .isEqualTo(MOCK_RESULT);
  }


  @Test
  public void proxy_dataNotFound() {
    when(resolver.resolve(NAMESPACE, LOOKUP, ID)).thenReturn(Optional.empty());
    when(supplier.get()).thenReturn(REAL_RESULT);

    assertThat(manager.proxy(NAMESPACE, LOOKUP, ID, Object.class, supplier))
        .isNotNull()
        .isEqualTo(REAL_RESULT);
  }


}