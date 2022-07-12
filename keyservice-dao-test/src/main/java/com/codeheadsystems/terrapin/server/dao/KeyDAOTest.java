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

package com.codeheadsystems.terrapin.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.metrics.test.BaseMetricTest;
import com.codeheadsystems.terrapin.common.factory.ObjectMapperFactory;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKey;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class KeyDAOTest extends BaseMetricTest {

    protected final Random random = new Random();
    protected final ObjectMapper mapper = new ObjectMapperFactory().generate();
    protected KeyDAO dao;

    protected abstract KeyDAO keyDAO();

    @BeforeEach
    void setupDao() {
        dao = keyDAO();
    }

    @Test
    public void assertTrue() {
        assertThat(keyDAO())
                .isNotNull();
    }

    @Test
    public void store_load() {
        final Key key = getKey();
        dao.store(key);
        final Optional<Key> result = dao.load(key.keyIdentifier());

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .get()
                .isEqualTo(key);
    }

    @Test
    public void load_notFound() {
        final Key key = getKey();
        final Optional<Key> result = dao.load(key.keyIdentifier());

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void change_active_key() {
        final Key key = getKey();
        final boolean initialActiveState = key.active();
        dao.store(key);
        assertThat(dao.load(key.keyIdentifier()))
                .isNotEmpty()
                .get()
                .hasFieldOrPropertyWithValue("active", initialActiveState);

        final boolean newActiveState = !initialActiveState;
        final Key firstUpdatedKey = ImmutableKey.copyOf(key).withActive(newActiveState);
        dao.store(firstUpdatedKey);
        assertThat(dao.load(key.keyIdentifier()))
                .isNotEmpty()
                .get()
                .hasFieldOrPropertyWithValue("active", newActiveState);

        final Key secondUpdatedKey = ImmutableKey.copyOf(firstUpdatedKey).withActive(initialActiveState);
        dao.store(secondUpdatedKey);
        assertThat(dao.load(key.keyIdentifier()))
                .isNotEmpty()
                .get()
                .hasFieldOrPropertyWithValue("active", initialActiveState);
    }

    protected Key getKey() {
        final InputStream stream = KeyDAOTest.class.getClassLoader().getResourceAsStream("fixture/Key.json");
        try {
            final Key key = mapper.readValue(stream, Key.class);
            final byte[] value = new byte[32];
            random.nextBytes(value);
            return ImmutableKey.copyOf(key).withValue(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
