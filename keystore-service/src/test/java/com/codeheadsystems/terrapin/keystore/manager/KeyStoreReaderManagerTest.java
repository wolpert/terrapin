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

package com.codeheadsystems.terrapin.keystore.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.terrapin.server.dao.KeyDao;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KeyStoreReaderManagerTest {

  @Mock private KeyDao keyDAO;

  @Mock private KeyVersionIdentifier keyVersionIdentifier;
  @Mock private KeyIdentifier keyIdentifier;
  @Mock private Key key;

  @Captor private ArgumentCaptor<byte[]> byteCapture;

  private KeyStoreReaderManager manager;

  @BeforeEach
  public void setup() {
    manager = new KeyStoreReaderManager(keyDAO);
  }

  @Test
  void getKey_keyVersion() {
    when(keyDAO.load(keyIdentifier)).thenReturn(Optional.of(key));
    assertThat(manager.getKey(keyIdentifier))
        .isNotEmpty()
        .get()
        .isEqualTo(key);
  }

  @Test
  void getKey_keyVersionIdentifier() {
    when(keyDAO.load(keyVersionIdentifier)).thenReturn(Optional.of(key));
    assertThat(manager.getKey(keyVersionIdentifier))
        .isNotEmpty()
        .get()
        .isEqualTo(key);
  }

}