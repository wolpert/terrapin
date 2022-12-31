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

package com.codeheadsystems.keystore.server.dao;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import com.codeheadsystems.keystore.common.factory.ObjectMapperFactory;
import com.codeheadsystems.keystore.server.dao.model.*;
import com.codeheadsystems.metrics.test.BaseMetricTest;
import com.codeheadsystems.test.unique.UniqueString;
import com.codeheadsystems.test.unique.UniqueStringExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Random;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(UniqueStringExtension.class)
public abstract class KeyDaoTest extends BaseMetricTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyDaoTest.class);
  protected final static Random random = new Random();
  protected final ObjectMapper mapper = new ObjectMapperFactory().generate();
  protected KeyDao dao;

  @UniqueString(prefix = "owner", separator = ".")
  protected String owner;
  protected abstract KeyDao keyDAO();

  @BeforeEach
  void setupDao() {
    dao = keyDAO();
  }
  
  protected String owner() {
    return owner;
  }

  @Test
  public void assertTrue() {
    LOGGER.info("assertTrue -->");
    assertThat(keyDAO())
        .isNotNull();
  }

  @Test
  public void store_load() {
    LOGGER.info("store_load -->");
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
    LOGGER.info("load_notFound -->");
    final Key key = getKey();
    final Optional<Key> result = dao.load(key.keyVersionIdentifier()); // key version identifier

    assertThat(result)
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void loadActive_oneKey() {
    LOGGER.info("loadActive_oneKey -->");
    final Key key = getKey(true, 2);
    final KeyIdentifier keyIdentifier = getKeyIdentifier(key);
    dao.store(key);
    assertQueryReturnsKey(key);
  }

  @Test
  public void loadActive_oneKey_notActive() {
    LOGGER.info("loadActive_oneKey_notActive -->");
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
    LOGGER.info("loadActive_threeKeys_twoActive -->");
    final Key key1 = getAndStoreKey(true, 3);
    final Key key2 = getAndStoreKey(true, 2);
    final Key key3 = getAndStoreKey(false, 1);
    assertQueryReturnsKey(key1);
  }

  @Test
  public void loadActive_threeKeys_twoActive_diffOrder() {
    LOGGER.info("loadActive_threeKeys_twoActive_diffOrder -->");
    final Key key2 = getAndStoreKey(true, 2);
    final Key key3 = getAndStoreKey(false, 1);
    final Key key1 = getAndStoreKey(true, 3);
    assertQueryReturnsKey(key1);
  }

  @Test
  public void loadActive_threeKeys_oneActive() {
    LOGGER.info("loadActive_threeKeys_oneActive -->");
    final Key key2 = getAndStoreKey(false, 3);
    final Key key1 = getAndStoreKey(true, 2);
    final Key key3 = getAndStoreKey(false, 1);
    assertQueryReturnsKey(key1);
  }

  @Test
  public void loadActive_threeKeys_zeroActive() {
    LOGGER.info("loadActive_threeKeys_zeroActive -->");
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
    LOGGER.info("loadOwner_found -->");
    final OwnerIdentifier owner = dao.storeOwner(owner());
    final Optional<OwnerIdentifier> result = dao.loadOwner(owner());
    assertThat(result)
        .isNotNull()
        .isNotEmpty()
        .get()
        .hasFieldOrPropertyWithValue("owner", owner())
        .isEqualTo(owner);
  }

  @Test
  public void loadOwner_notfound() {
    LOGGER.info("loadOwner_notfound -->");
    final Optional<OwnerIdentifier> result = dao.loadOwner(owner());
    assertThat(result)
        .isNotNull()
        .isEmpty();
  }


  @Test
  public void change_active_key() {
    LOGGER.info("change_active_key -->");
    final Key key = getKey();
    final boolean initialActiveState = key.active();
    dao.store(key);
    Assertions.assertThat(dao.load(key.keyVersionIdentifier()))
        .isNotEmpty()
        .get()
        .hasFieldOrPropertyWithValue("active", initialActiveState);

    final boolean newActiveState = !initialActiveState;
    final Key firstUpdatedKey = ImmutableKey.copyOf(key).withActive(newActiveState);
    dao.store(firstUpdatedKey);
    Assertions.assertThat(dao.load(key.keyVersionIdentifier()))
        .isNotEmpty()
        .get()
        .hasFieldOrPropertyWithValue("active", newActiveState);

    final Key secondUpdatedKey = ImmutableKey.copyOf(firstUpdatedKey).withActive(initialActiveState);
    dao.store(secondUpdatedKey);
    Assertions.assertThat(dao.load(key.keyVersionIdentifier()))
        .isNotEmpty()
        .get()
        .hasFieldOrPropertyWithValue("active", initialActiveState);
  }

  @Test
  public void listKeys() {
    LOGGER.info("listKeys -->");
    final Key key1 = getAndStoreKey(true, 1, "fred");
    final KeyIdentifier identifier = ImmutableKeyIdentifier.copyOf(key1.keyVersionIdentifier());
    getAndStoreKey(true, 2, "fred");
    final Batch<KeyIdentifier> keys = dao.listKeys(ImmutableOwnerIdentifier.builder().owner("fred").build(), null);
    assertThat(keys)
        .isNotNull()
        .hasFieldOrPropertyWithValue("nextToken", null)
        .extracting("list", as(LIST))
        .isNotEmpty()
        .hasSize(1)
        .containsExactly(identifier);
  }

  @Test
  public void listOwners() {
    LOGGER.info("listOwners -->");
    final Key key1 = getAndStoreKey(true, 1, "fred");
    final Key key2 = getAndStoreKey(true, 1, "barney");
    final Key key3 = getAndStoreKey(true, 1, "smith");
    final OwnerIdentifier o1 = dao.storeOwner("fred");
    final OwnerIdentifier o2 = dao.storeOwner("barney");
    final OwnerIdentifier o3 = dao.storeOwner("smith");
    final OwnerIdentifier o4 = dao.storeOwner("sam");
    final Batch<OwnerIdentifier> ownerIdentifierBatch = dao.listOwners(null);
    assertThat(ownerIdentifierBatch)
        .isNotNull()
        .hasFieldOrPropertyWithValue("nextToken", null)
        .extracting("list", as(LIST))
        .isNotEmpty()
        .hasSizeGreaterThanOrEqualTo(4)
        .contains(o1, o2, o3, o4);
  }

  @Test
  public void listKeyVersions() {
    LOGGER.info("listKeyVersions -->");
    final Key key1 = getAndStoreKey(false, 1);
    final Key key2 = getAndStoreKey(true, 2);
    final Key key3 = getAndStoreKey(true, 3);
    final Batch<KeyVersionIdentifier> result = dao.listVersions(key1.keyVersionIdentifier(), null);
    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("nextToken", null)
        .extracting("list", as(LIST))
        .isNotEmpty()
        .hasSize(3)
        .containsOnly(key1.keyVersionIdentifier(), key2.keyVersionIdentifier(), key3.keyVersionIdentifier());
  }

  @Test
  public void deleteKeyVersion() {
    LOGGER.info("deleteKeyVersion -->");
    final Key key1 = getAndStoreKey(false, 1);
    final Key key2 = getAndStoreKey(true, 2);
    final Key key3 = getAndStoreKey(true, 3);
    Assertions.assertThat(dao.load(key1.keyVersionIdentifier()))
        .isNotEmpty()
        .get()
        .isEqualTo(key1);
    final Batch<KeyVersionIdentifier> result = dao.listVersions(key1.keyVersionIdentifier(), null);
    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("nextToken", null)
        .extracting("list", as(LIST))
        .isNotEmpty()
        .hasSize(3)
        .containsOnly(key1.keyVersionIdentifier(), key2.keyVersionIdentifier(), key3.keyVersionIdentifier());

    dao.delete(key1.keyVersionIdentifier());
    Assertions.assertThat(dao.load(key1.keyVersionIdentifier()))
        .isEmpty();

    final Batch<KeyVersionIdentifier> result2 = dao.listVersions(key1.keyVersionIdentifier(), null);
    assertThat(result2)
        .isNotNull()
        .hasFieldOrPropertyWithValue("nextToken", null)
        .extracting("list", as(LIST))
        .isNotEmpty()
        .hasSize(2)
        .containsOnly(key2.keyVersionIdentifier(), key3.keyVersionIdentifier());
  }

  // Disabled because this is really a batch process.
  //@Test
  public void deleteAllKeyVersions() {
    LOGGER.info("deleteAllKeyVersions -->");
    final Key key1 = getAndStoreKey(false, 1);
    final Key key2 = getAndStoreKey(true, 2);
    final Key key3 = getAndStoreKey(true, 3);
    final KeyIdentifier identifier = ImmutableKeyIdentifier.copyOf(key1.keyVersionIdentifier());
    final Batch<KeyVersionIdentifier> result = dao.listVersions(key1.keyVersionIdentifier(), null);
    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("nextToken", null)
        .extracting("list", as(LIST))
        .isNotEmpty()
        .hasSize(3)
        .containsOnly(key1.keyVersionIdentifier(), key2.keyVersionIdentifier(), key3.keyVersionIdentifier());

    dao.delete(identifier);
    Assertions.assertThat(dao.load(key1.keyVersionIdentifier()))
        .isEmpty();
    Assertions.assertThat(dao.load(key2.keyVersionIdentifier()))
        .isEmpty();
    Assertions.assertThat(dao.load(key3.keyVersionIdentifier()))
        .isEmpty();

    final Batch<KeyVersionIdentifier> result2 = dao.listVersions(key1.keyVersionIdentifier(), null);
    assertThat(result2)
        .isNotNull()
        .hasFieldOrPropertyWithValue("nextToken", null)
        .extracting("list", as(LIST))
        .isEmpty();
  }

  private Key getKey(final boolean active,
                     final long version,
                     final String owner) {
    final InputStream stream = KeyDaoTest.class.getClassLoader().getResourceAsStream("fixture/Key.json");
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
    return getKey(active, version, owner());
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
