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

package com.codeheadsystems.keystore.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.codeheadsystems.keystore.api.Key;
import com.codeheadsystems.keystore.converter.ApiConverter;
import com.codeheadsystems.keystore.exception.AlreadyExistsException;
import com.codeheadsystems.keystore.manager.KeyStoreAdminManager;
import com.codeheadsystems.keystore.server.dao.model.KeyIdentifier;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KeyManagerResourceTest {

  public static final String KEY_ID = "keyId";
  public static final String OWNER = "owner";
  @Mock private ApiConverter apiConverter;
  @Mock private KeyStoreAdminManager keyStoreAdminManager;

  @Mock private Key apiKey;
  @Mock private com.codeheadsystems.keystore.server.dao.model.Key modelKey;
  @Mock private KeyIdentifier keyIdentifier;


  private KeyManagerResource resource;

  @BeforeEach
  public void setup(){
    resource = new KeyManagerResource(apiConverter, keyStoreAdminManager);
  }

  @Test
  public void create_noError() throws AlreadyExistsException {
    when(apiConverter.toDaoKeyIdentifier(OWNER, KEY_ID)).thenReturn(keyIdentifier);
    when(keyStoreAdminManager.create(keyIdentifier)).thenReturn(modelKey);
    when(apiConverter.toApiKey(modelKey)).thenReturn(apiKey);

    final Key result = resource.create(OWNER, KEY_ID);

    assertThat(result)
        .isNotNull()
        .isEqualTo(apiKey);
  }

  @Test
  public void create_alreadyExist() throws AlreadyExistsException {
    when(apiConverter.toDaoKeyIdentifier(OWNER, KEY_ID)).thenReturn(keyIdentifier);
    when(keyStoreAdminManager.create(keyIdentifier)).thenThrow(new AlreadyExistsException());

    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> resource.create(OWNER, KEY_ID))
        .withMessageContaining("already exists")
        .extracting("response")
        .hasFieldOrPropertyWithValue("status", 409);
  }

}