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

import static com.codeheadsystems.terrapin.server.dao.converter.KeyConverter.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.terrapin.common.helper.DataHelper;
import com.codeheadsystems.terrapin.keystore.exception.AlreadyExistsException;
import com.codeheadsystems.terrapin.keystore.model.RNG;
import com.codeheadsystems.terrapin.server.dao.KeyDAO;
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
class KeyManagerTest {
    public static final String OWNER = "fred";
    public static final String KEY_ID = "somekey";

    @Mock private KeyDAO keyDAO;
    @Mock private RNG rng;

    @Mock private KeyVersionIdentifier keyVersionIdentifier;
    @Mock private KeyIdentifier keyIdentifier;
    @Mock private Key key;
    @Mock private DataHelper dataHelper;

    @Captor private ArgumentCaptor<byte[]> byteCapture;

    private KeyManager manager;

    @BeforeEach
    public void setup() {
        manager = new KeyManager(keyDAO, rng, dataHelper);
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
        verify(rng).random(byteCapture.capture());
        verify(dataHelper).clear(byteCapture.capture());
    }

}