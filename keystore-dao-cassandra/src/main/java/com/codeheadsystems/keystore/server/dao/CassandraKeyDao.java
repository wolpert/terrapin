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

import com.codeheadsystems.keystore.server.dao.casssandra.accessor.CassandraAccessor;
import com.codeheadsystems.keystore.server.dao.casssandra.converter.KeyConverter;
import com.codeheadsystems.keystore.server.dao.casssandra.converter.OwnerConverter;
import com.codeheadsystems.keystore.server.dao.casssandra.dagger.StatementModule;
import com.codeheadsystems.keystore.server.dao.casssandra.manager.BoundStatementManager;
import com.codeheadsystems.keystore.server.dao.model.Batch;
import com.codeheadsystems.keystore.server.dao.model.ImmutableBatch;
import com.codeheadsystems.keystore.server.dao.model.ImmutableOwnerIdentifier;
import com.codeheadsystems.keystore.server.dao.model.Key;
import com.codeheadsystems.keystore.server.dao.model.KeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.KeyVersionIdentifier;
import com.codeheadsystems.keystore.server.dao.model.OwnerIdentifier;
import com.codeheadsystems.keystore.server.dao.model.Token;
import com.codeheadsystems.metrics.Metrics;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO for Cassandra.
 */
@Singleton
public class CassandraKeyDao implements KeyDao {

  /**
   * The constant OWNER.
   */
  public static final String OWNER = "owner";
  /**
   * The constant PREFIX.
   */
  public static final String PREFIX = "ddbdao.";
  /**
   * The constant MAX_TIMES_KEY_STORE.
   */
  public static final int MAX_TIMES_KEY_STORE = 5;
  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraKeyDao.class);
  private final CassandraAccessor cassandraAccessor;
  private final Metrics metrics;
  private final BoundStatementManager binder;
  private final OwnerConverter ownerConverter;
  private final KeyConverter keyConverter;

  /**
   * Default constructor.
   *
   * @param cassandraAccessor to wrap downstream calls with retries.
   * @param metrics           metrics for reporting.
   * @param binder            binder for the prepared statements.
   * @param ownerConverter    Owner converter.
   * @param keyConverter      key convertor.
   */
  @Inject
  public CassandraKeyDao(final CassandraAccessor cassandraAccessor,
                         final Metrics metrics,
                         final BoundStatementManager binder,
                         final OwnerConverter ownerConverter,
                         final KeyConverter keyConverter) {
    LOGGER.info("CassandraKeyDAO({},{})", cassandraAccessor, metrics);
    this.binder = binder;
    this.cassandraAccessor = cassandraAccessor;
    this.metrics = metrics;
    this.ownerConverter = ownerConverter;
    this.keyConverter = keyConverter;
  }

  private <T> T time(final String methodName,
                     final String owner,
                     final Supplier<T> supplier) {
    final String name = PREFIX + methodName;
    final Timer timer = metrics.registry().timer(name, OWNER, (owner == null ? "null" : owner));
    // TODO: Vet cardinality. Set by configuration?
    return metrics.time(name, timer, supplier);
  }

  @Override
  public void store(final Key key) {
    LOGGER.debug("store({})", key.keyVersionIdentifier());
    time("storeKey", key.keyVersionIdentifier().owner(), () -> {
      cassandraAccessor.execute(binder.bind(StatementModule.KEY_STORE_STMT, key));
      if (key.active()) {
        cassandraAccessor.execute(binder.bind(StatementModule.KEY_STORE_ACTIVE_STMT, key));
      } else {
        cassandraAccessor.execute(binder.bind(StatementModule.KEY_DELETE_ACTIVE_STMT, key.keyVersionIdentifier()));
      }
      cassandraAccessor.execute(binder.bind(StatementModule.OWNER_STORE_KEY_STMT, key));
      return null;
    });
  }

  @Override
  public OwnerIdentifier storeOwner(final String owner) {
    LOGGER.debug("storeOwner({})", owner);
    return time("storeOwner", owner, () -> {
      final Statement<?> statement = binder.bind(StatementModule.OWNER_STORE_STMT, owner);
      cassandraAccessor.execute(statement);
      return ImmutableOwnerIdentifier.builder().owner(owner).build();
    });
  }

  @Override
  public Optional<Key> load(final KeyVersionIdentifier identifier) {
    LOGGER.debug("load({})", identifier);
    return time("loadKeyVersion", identifier.owner(), () -> {
      final ResultSet resultSet = cassandraAccessor
          .execute(binder.bind(StatementModule.KEY_LOAD_VERSION_STMT, identifier));
      final Row row = resultSet.one();
      if (row == null) {
        return Optional.empty();
      } else {
        return Optional.of(keyConverter.toKey(row));
      }
    });
  }

  /**
   * Query against the active hash, returning the key with the greatest number.
   * Empty optional if there is no active key or if there is no keys in general.
   */
  @Override
  public Optional<Key> load(final KeyIdentifier identifier) {
    LOGGER.debug("load({})", identifier);
    return time("loadKey", identifier.owner(), () -> {
      final ResultSet resultSet = cassandraAccessor
          .execute(binder.bind(StatementModule.KEY_LOAD_ACTIVE_VERSION_STMT, identifier));
      final Row row = resultSet.one();
      if (row == null) {
        return Optional.empty();
      } else {
        return Optional.of(keyConverter.toKey(row));
      }
    });
  }

  @Override
  public Optional<OwnerIdentifier> loadOwner(final String ownerName) {
    LOGGER.debug("loadOwner({})", ownerName);
    return time("loadOwner", ownerName, () -> {
      final Statement<?> statement = binder.bind(StatementModule.OWNER_LOAD_STMT, ownerName);
      final ResultSet resultSet = cassandraAccessor.execute(statement);
      final Row row = resultSet.one();
      if (row == null) {
        return Optional.empty();
      } else {
        return Optional.of(ownerConverter.toOwnerIdentifier(row));
      }
    });
  }

  @Override
  public Batch<OwnerIdentifier> listOwners(final Token nextToken) {
    LOGGER.debug("listOwners()");
    return time("listOwners", null, () -> {
      final ResultSet resultSet = cassandraAccessor.execute(binder.bind(StatementModule.OWNER_LIST_STMT, null));
      // TODO: Do this in a batching way
      final List<OwnerIdentifier> list = resultSet.all().stream()
          .map(ownerConverter::toOwnerIdentifier)
          .toList();
      return ImmutableBatch.<OwnerIdentifier>builder().list(list).build();
    });
  }


  @Override
  public Batch<KeyIdentifier> listKeys(final OwnerIdentifier identifier,
                                       final Token nextToken) {
    LOGGER.debug("listKeys({})", identifier);
    return time("listKeys", identifier.owner(), () -> {
      final ResultSet resultSet = cassandraAccessor.execute(binder.bind(StatementModule.KEY_LIST_STMT, identifier));
      // TODO: Do this in a batching way
      final List<KeyIdentifier> list = resultSet.all().stream()
          .map(ownerConverter::toKeyIdentifier)
          .filter(ki -> !ki.key().equals(StatementModule.DETAILS))
          .toList();
      return ImmutableBatch.<KeyIdentifier>builder().list(list).build();
    });
  }

  @Override
  public Batch<KeyVersionIdentifier> listVersions(final KeyIdentifier identifier,
                                                  final Token nextToken) {
    LOGGER.debug("listVersions({})", identifier);
    return time("listVersions", identifier.owner(), () -> {
      final ResultSet resultSet = cassandraAccessor
          .execute(binder.bind(StatementModule.KEY_LIST_VERSION_STMT, identifier));
      // TODO: Do this in a batching way
      final List<KeyVersionIdentifier> list = resultSet.all().stream()
          .map(keyConverter::toKeyVersionIdentifier)
          .toList();
      return ImmutableBatch.<KeyVersionIdentifier>builder().list(list).build();
    });
  }

  @Override
  public boolean delete(final KeyVersionIdentifier identifier) {
    LOGGER.debug("delete({})", identifier);
    return time("deleteVersions", identifier.owner(), () -> {
      cassandraAccessor.execute(binder.bind(StatementModule.KEY_DELETE_ACTIVE_STMT, identifier));
      cassandraAccessor.execute(binder.bind(StatementModule.KEY_DELETE_VERSION_STMT, identifier));
      return false;
    });
  }

  @Override
  public boolean delete(final KeyIdentifier identifier) {
    LOGGER.debug("delete({})", identifier);
    return time("deleteKey", identifier.owner(), () -> {
      return false;
    });
  }

  @Override
  public boolean delete(final OwnerIdentifier identifier) {
    LOGGER.debug("delete({})", identifier);
    return time("deleteOwner", identifier.owner(), () -> {
      return false;
    });
  }
}
