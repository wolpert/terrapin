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

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import com.codeheadsystems.metrics.test.BaseMetricTest;
import com.codeheadsystems.terrapin.common.factory.ObjectMapperFactory;
import com.codeheadsystems.terrapin.server.dao.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
        final Optional<Key> result = dao.load(key.keyVersionIdentifier()); // key version identifier

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .get()
                .isEqualTo(key);
    }

    @Test
    public void load_notFound() {
        final Key key = getKey();
        final Optional<Key> result = dao.load(key.keyVersionIdentifier()); // key version identifier

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void loadActive_oneKey() {
        final Key key = getKey(true, 2);
        final KeyIdentifier keyIdentifier = getKeyIdentifier(key);
        dao.store(key);
        assertQueryReturnsKey(key);
    }

    @Test
    public void loadActive_oneKey_notActive() {
        final Key key = getKey(false, 1);
        final KeyIdentifier keyIdentifier = getKeyIdentifier(key);
        dao.store(key);
        final Optional<Key> result = dao.load(keyIdentifier);
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void loadActive_threeKeys_twoActive() {
        final Key key1 = getAndStoreKey(true, 3);
        final Key key2 = getAndStoreKey(true, 2);
        final Key key3 = getAndStoreKey(false, 1);
        assertQueryReturnsKey(key1);
    }

    @Test
    public void loadActive_threeKeys_twoActive_diffOrder() {
        final Key key2 = getAndStoreKey(true, 2);
        final Key key3 = getAndStoreKey(false, 1);
        final Key key1 = getAndStoreKey(true, 3);
        assertQueryReturnsKey(key1);
    }

    @Test
    public void loadActive_threeKeys_oneActive() {
        final Key key2 = getAndStoreKey(false, 3);
        final Key key1 = getAndStoreKey(true, 2);
        final Key key3 = getAndStoreKey(false, 1);
        assertQueryReturnsKey(key1);
    }

    @Test
    public void loadActive_threeKeys_zeroActive() {
        final Key key3 = getAndStoreKey(false, 1);
        final Key key1 = getAndStoreKey(false, 3);
        final Key key2 = getAndStoreKey(false, 2);
        final KeyIdentifier keyIdentifier = getKeyIdentifier(key3);
        final Optional<Key> result = dao.load(keyIdentifier);
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void loadOwner_found() {
        final Key key = getAndStoreKey(true, 1);
        final String owner = key.keyVersionIdentifier().owner();
        final Optional<OwnerIdentifier> result = dao.loadOwner(owner);
        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .get()
                .hasFieldOrPropertyWithValue("owner", owner);
    }

    @Test
    public void loadOwner_notfound() {
        final Key key = getAndStoreKey(true, 1);
        final String owner = key.keyVersionIdentifier().owner() + "someoneelse";
        final Optional<OwnerIdentifier> result = dao.loadOwner(owner);
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }


    @Test
    public void change_active_key() {
        final Key key = getKey();
        final boolean initialActiveState = key.active();
        dao.store(key);
        assertThat(dao.load(key.keyVersionIdentifier()))
                .isNotEmpty()
                .get()
                .hasFieldOrPropertyWithValue("active", initialActiveState);

        final boolean newActiveState = !initialActiveState;
        final Key firstUpdatedKey = ImmutableKey.copyOf(key).withActive(newActiveState);
        dao.store(firstUpdatedKey);
        assertThat(dao.load(key.keyVersionIdentifier()))
                .isNotEmpty()
                .get()
                .hasFieldOrPropertyWithValue("active", newActiveState);

        final Key secondUpdatedKey = ImmutableKey.copyOf(firstUpdatedKey).withActive(initialActiveState);
        dao.store(secondUpdatedKey);
        assertThat(dao.load(key.keyVersionIdentifier()))
                .isNotEmpty()
                .get()
                .hasFieldOrPropertyWithValue("active", initialActiveState);
    }

    @Test
    public void listKeys() {
        getAndStoreKey(true, 1, "fred");
        getAndStoreKey(true, 2, "fred");
        final Batch<KeyIdentifier> keys = dao.listKeys(ImmutableOwnerIdentifier.builder().owner("fred").build(), null);
        assertThat(keys)
                .isNotNull()
                .hasFieldOrPropertyWithValue("nextToken", null)
                .extracting("list", as(LIST))
                .isNotEmpty()
                .hasSize(1);
    }

    private Key getKey(final boolean active,
                       final long version,
                       final String owner) {
        final InputStream stream = KeyDAOTest.class.getClassLoader().getResourceAsStream("fixture/Key.json");
        try {
            final Key key = mapper.readValue(stream, Key.class);
            final byte[] value = new byte[32];
            random.nextBytes(value);
            final KeyVersionIdentifier identifier = ImmutableKeyVersionIdentifier.copyOf(key.keyVersionIdentifier())
                    .withVersion(version)
                    .withOwner(owner);
            return ImmutableKey.copyOf(key)
                    .withValue(value)
                    .withActive(active)
                    .withKeyVersionIdentifier(identifier);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Key getKey() {
        return getKey(true, 2);
    }

    private Key getKey(final boolean active, final long version) {
        return getKey(active, version, "owner");
    }

    public Key getAndStoreKey(final boolean active,
                              final long version,
                              final String owner) {
        final Key key = getKey(active, version, owner);
        dao.store(key);
        return key;
    }

    public Key getAndStoreKey(final boolean active,
                              final long version) {
        final Key key = getKey(active, version);
        dao.store(key);
        return key;
    }

    private KeyIdentifier getKeyIdentifier(final Key key) {
        final KeyIdentifier keyIdentifier = ImmutableKeyIdentifier.builder()
                .owner(key.keyVersionIdentifier().owner())
                .key(key.keyVersionIdentifier().key())
                .build();
        return keyIdentifier;
    }

    private void assertQueryReturnsKey(final Key key) {
        final KeyIdentifier keyIdentifier = getKeyIdentifier(key);
        final Optional<Key> result = dao.load(keyIdentifier);
        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .get()
                .isEqualTo(key);
    }
}
