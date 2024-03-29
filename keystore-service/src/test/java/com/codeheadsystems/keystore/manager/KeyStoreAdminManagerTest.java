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

package com.codeheadsystems.keystore.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.keystore.common.helper.DataHelper;
import com.codeheadsystems.keystore.common.model.Rng;
import com.codeheadsystems.keystore.exception.AlreadyExistsException;
import com.codeheadsystems.keystore.server.dao.KeyDao;
import com.codeheadsystems.keystore.server.dao.model.Key;
import com.codeheadsystems.keystore.server.dao.model.KeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.KeyVersionIdentifier;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KeyStoreAdminManagerTest {
  public static final String OWNER = "fred";
  public static final String KEY_ID = "somekey";

  @Mock private KeyDao keyDAO;
  @Mock private Rng rng;

  @Mock private KeyVersionIdentifier keyVersionIdentifier;
  @Mock private KeyIdentifier keyIdentifier;
  @Mock private Key key;
  @Mock private DataHelper dataHelper;

  @Captor private ArgumentCaptor<byte[]> byteCapture;

  private KeyStoreAdminManager manager;

  @BeforeEach
  public void setup() {
    manager = new KeyStoreAdminManager(keyDAO, rng, dataHelper);
  }

  @Test
  void create_existingKey() {
    when(keyDAO.load(keyIdentifier)).thenReturn(Optional.of(key));

    assertThatExceptionOfType(AlreadyExistsException.class)
        .isThrownBy(() -> manager.create(keyIdentifier));
  }

  @Test
  void create_newKey() throws AlreadyExistsException {
    when(keyDAO.load(keyIdentifier)).thenReturn(Optional.empty());
    when(keyIdentifier.owner()).thenReturn(OWNER);
    when(keyIdentifier.key()).thenReturn(KEY_ID);

    final Key result = manager.create(keyIdentifier);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("active", true)
        .extracting("keyVersionIdentifier")
        .hasFieldOrPropertyWithValue("owner", OWNER)
        .hasFieldOrPropertyWithValue("key", KEY_ID)
        .hasFieldOrPropertyWithValue("version", 1L);
    verify(rng, times(2)).random(byteCapture.capture());
    verify(dataHelper).clear(byteCapture.capture());
  }

}