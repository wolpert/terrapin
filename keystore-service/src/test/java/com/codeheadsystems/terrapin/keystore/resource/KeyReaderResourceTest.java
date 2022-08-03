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

package com.codeheadsystems.terrapin.keystore.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.codeheadsystems.terrapin.keystore.converter.ApiConverter;
import com.codeheadsystems.terrapin.keystore.manager.KeyStoreReaderManager;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class KeyReaderResourceTest {

    public static final String OWNER = "fred";
    public static final String KEY_ID = "somekey";
    public static final long VERSION = 10L;

    @Mock private ApiConverter apiConverter;
    @Mock private KeyStoreReaderManager keyStoreReaderManager;

    @Mock private Key daoKey;
    @Mock private KeyIdentifier keyIdentifier;
    @Mock private KeyVersionIdentifier keyVersionIdentifier;
    @Mock private com.codeheadsystems.terrapin.keystore.api.Key apiKey;

    private KeyReaderResource resource;

    @BeforeEach
    void setup() {
        resource = new KeyReaderResource(apiConverter, keyStoreReaderManager);
    }

    @Test
    void get_ownerKey_found() {
        when(apiConverter.toDaoKeyIdentifier(OWNER, KEY_ID)).thenReturn(keyIdentifier);
        when(keyStoreReaderManager.getKey(keyIdentifier)).thenReturn(Optional.of(daoKey));
        when(apiConverter.toApiKey(daoKey)).thenReturn(apiKey);

        assertThat(resource.get(OWNER, KEY_ID))
                .isNotNull()
                .isEqualTo(apiKey);
    }

    @Test
    void get_ownerKey_notFound() {
        when(apiConverter.toDaoKeyIdentifier(OWNER, KEY_ID)).thenReturn(keyIdentifier);

        assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> resource.get(OWNER, KEY_ID))
                .extracting("response")
                .hasFieldOrPropertyWithValue("status", 404);
    }

    @Test
    void get_ownerKeyVersion_found() {
        when(apiConverter.toDaoKeyVersionIdentifier(OWNER, KEY_ID, VERSION)).thenReturn(keyVersionIdentifier);
        when(keyStoreReaderManager.getKey(keyVersionIdentifier)).thenReturn(Optional.of(daoKey));
        when(apiConverter.toApiKey(daoKey)).thenReturn(apiKey);

        assertThat(resource.get(OWNER, KEY_ID, VERSION))
                .isNotNull()
                .isEqualTo(apiKey);
    }

    @Test
    void get_ownerKeyVersion_notFound() {
        when(apiConverter.toDaoKeyVersionIdentifier(OWNER, KEY_ID, VERSION)).thenReturn(keyVersionIdentifier);

        assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> resource.get(OWNER, KEY_ID, VERSION))
                .extracting("response")
                .hasFieldOrPropertyWithValue("status", 404);
    }

}