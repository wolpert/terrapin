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

package com.codeheadsystems.keystore.server.dao.casssandra.dagger;

import com.codeheadsystems.keystore.server.dao.casssandra.configuration.TableConfiguration;
import com.codeheadsystems.keystore.server.dao.casssandra.manager.StatementBinder;
import com.codeheadsystems.keystore.server.dao.casssandra.manager.TimestampManager;
import com.codeheadsystems.keystore.server.dao.model.Key;
import com.codeheadsystems.keystore.server.dao.model.KeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.KeyVersionIdentifier;
import com.codeheadsystems.keystore.server.dao.model.OwnerIdentifier;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.inject.Singleton;

/**
 * All the cassandra prepared statements.
 */
@Module
public class StatementModule {

  /**
   * The constant DETAILS.
   */
  public static final String DETAILS = "details";
  /**
   * The constant OWNER_STORE_STMT.
   */
  public static final String OWNER_STORE_STMT = "owner.store";
  /**
   * The constant OWNER_STORE_KEY_STMT.
   */
  public static final String OWNER_STORE_KEY_STMT = "owner.store.key";
  /**
   * The constant OWNER_LOAD_STMT.
   */
  public static final String OWNER_LOAD_STMT = "owner.load";
  /**
   * The constant OWNER_LOAD_KEY_STMT.
   */
  public static final String OWNER_LOAD_KEY_STMT = "owner.load.key";
  /**
   * The constant KEY_STORE_STMT.
   */
  public static final String KEY_STORE_STMT = "key.store";
  /**
   * The constant KEY_STORE_ACTIVE_STMT.
   */
  public static final String KEY_STORE_ACTIVE_STMT = "key.store.active";
  /**
   * The constant KEY_DELETE_ACTIVE_STMT.
   */
  public static final String KEY_DELETE_ACTIVE_STMT = "key.delete.active";
  /**
   * The constant KEY_DELETE_VERSION_STMT.
   */
  public static final String KEY_DELETE_VERSION_STMT = "key.delete.version";
  /**
   * The constant KEY_LOAD_VERSION_STMT.
   */
  public static final String KEY_LOAD_VERSION_STMT = "key.load.version";
  /**
   * The constant KEY_LOAD_ACTIVE_VERSION_STMT.
   */
  public static final String KEY_LOAD_ACTIVE_VERSION_STMT = "key.load.active.version";
  /**
   * The constant KEY_LIST_VERSION_STMT.
   */
  public static final String KEY_LIST_VERSION_STMT = "key.list.version";
  /**
   * The constant KEY_LIST_STMT.
   */
  public static final String KEY_LIST_STMT = "key.list";
  /**
   * The constant OWNER_LIST_STMT.
   */
  public static final String OWNER_LIST_STMT = "owner.list";

  /**
   * Prepared statement: store owners.
   *
   * @param timestampManager   for managing times.
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(OWNER_STORE_STMT)
  public StatementBinder.Builder<?> ownerStore(final TimestampManager timestampManager,
                                               final TableConfiguration tableConfiguration) {
    final String baseInsert = "insert into %s.%s (owner, lookup, create_date) values (?,'%s',?)";
    final String insert = String.format(baseInsert,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable(), DETAILS);
    return StatementBinder.<String>builder().with(insert)
        .with((owner) -> new Object[]{owner, timestampManager.timestamp()});
  }


  /**
   * Prepared statement: store keys.
   *
   * @param timestampManager   for managing times.
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(OWNER_STORE_KEY_STMT)
  public StatementBinder.Builder<?> ownerStoreKey(final TimestampManager timestampManager,
                                                  final TableConfiguration tableConfiguration) {
    final String baseInsert = "insert into %s.%s (owner, lookup, create_date) values (?,?,?)";
    final String insert = String.format(baseInsert,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable());
    return StatementBinder.<Key>builder().with(insert).with((key) -> new Object[]{
        key.keyVersionIdentifier().owner(), key.keyVersionIdentifier().key(), timestampManager.timestamp()});
  }

  /**
   * Prepared Statement: load owner.
   *
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(OWNER_LOAD_STMT)
  public StatementBinder.Builder<?> ownerLoad(final TableConfiguration tableConfiguration) {
    final String baseSelect = "select * from %s.%s where owner = ? and lookup = '%s'";
    final String select = String.format(baseSelect,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable(), DETAILS);
    return StatementBinder.<String>builder()
        .with(select).with((owner) -> new Object[]{owner});
  }


  /**
   * Prepared Statement: load key from owner.
   *
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(OWNER_LOAD_KEY_STMT)
  public StatementBinder.Builder<?> ownerLoadKey(final TableConfiguration tableConfiguration) {
    final String baseSelect = "select * from %s.%s where owner = ? and lookup = '%s'";
    final String select = String.format(baseSelect,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable(), DETAILS);
    return StatementBinder.<KeyIdentifier>builder()
        .with(select).with((identifier) -> new Object[]{identifier.owner(), identifier.key()});
  }


  /**
   * Prepared Statement: load the key version.
   *
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(KEY_LOAD_VERSION_STMT)
  public StatementBinder.Builder<?> keyLoadVersion(final TableConfiguration tableConfiguration) {
    final String baseSelect = "select * from %s.%s where owner = ? and key_name = ? and version = ?";
    final String select = String.format(baseSelect,
        tableConfiguration.keyspace(), tableConfiguration.keysTable());
    return StatementBinder.<KeyVersionIdentifier>builder()
        .with(select).with((identifier) -> new Object[]{identifier.owner(), identifier.key(), identifier.version()});
  }


  /**
   * Prepared Statement: load the active key version.
   *
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(KEY_LOAD_ACTIVE_VERSION_STMT)
  public StatementBinder.Builder<?> keyLoadActiveVersion(final TableConfiguration tableConfiguration) {
    final String baseSelect = "select * from %s.%s where owner = ? and key_name = ? order by version desc limit 1";
    final String select = String.format(baseSelect,
        tableConfiguration.keyspace(), tableConfiguration.activeKeysTable());
    return StatementBinder.<KeyIdentifier>builder()
        .with(select).with((identifier) -> new Object[]{identifier.owner(), identifier.key()});
  }

  /**
   * Prepared Statement: list the key versions.
   *
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(KEY_LIST_VERSION_STMT)
  public StatementBinder.Builder<?> keyListVersions(final TableConfiguration tableConfiguration) {
    final String baseSelect = "select * from %s.%s where owner = ? and key_name = ? order by version desc";
    final String select = String.format(baseSelect,
        tableConfiguration.keyspace(), tableConfiguration.keysTable());
    return StatementBinder.<KeyIdentifier>builder()
        .with(select).with((identifier) -> new Object[]{identifier.owner(), identifier.key()});
  }

  /**
   * Prepared Statement: list the keys.
   *
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(KEY_LIST_STMT)
  public StatementBinder.Builder<?> keyList(final TableConfiguration tableConfiguration) {
    final String baseSelect = "select * from %s.%s where owner = ?";
    final String select = String.format(baseSelect,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable());
    return StatementBinder.<OwnerIdentifier>builder()
        .with(select).with((identifier) -> new Object[]{identifier.owner()});
  }

  /**
   * Prepared Statement: list the owners.
   *
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(OWNER_LIST_STMT)
  public StatementBinder.Builder<?> ownerList(final TableConfiguration tableConfiguration) {
    final String baseSelect = "select * from %s.%s where lookup = ?";
    final String select = String.format(baseSelect,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable());
    return StatementBinder.<Void>builder().with(select).with((identifier) -> new Object[]{DETAILS});
  }


  /**
   * Prepared statement: store the keys.
   *
   * @param timestampManager   for managing times.
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(KEY_STORE_STMT)
  public StatementBinder.Builder<?> storeKey(final TimestampManager timestampManager,
                                             final TableConfiguration tableConfiguration) {
    final String baseInsert = """
        insert into %s.%s 
          (owner, key_name, version, value, aux, active, type, create_date, update_date)
          values (?,?,?,?,?,?,?,?,?)""";
    final String insert = String.format(baseInsert,
        tableConfiguration.keyspace(), tableConfiguration.keysTable());
    return StatementBinder.<Key>builder().with(insert).with((key) -> new Object[]{
        key.keyVersionIdentifier().owner(), key.keyVersionIdentifier().key(), key.keyVersionIdentifier().version(),
        key.value(), key.aux(), key.active(), key.type(),
        timestampManager.fromDate(key.createDate()),
        key.updateDate().map(timestampManager::fromDate).orElse(null)
    });
  }

  /**
   * Prepared statement: store active keys.
   *
   * @param timestampManager   for managing times.
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(KEY_STORE_ACTIVE_STMT)
  public StatementBinder.Builder<?> storeActiveKey(final TimestampManager timestampManager,
                                                   final TableConfiguration tableConfiguration) {
    final String baseInsert = """
        insert into %s.%s 
          (owner, key_name, version, value, aux, active, type, create_date, update_date)
          values (?,?,?,?,?,?,?,?,?)""";
    final String insert = String.format(baseInsert,
        tableConfiguration.keyspace(), tableConfiguration.activeKeysTable());
    return StatementBinder.<Key>builder().with(insert).with((key) -> new Object[]{
        key.keyVersionIdentifier().owner(), key.keyVersionIdentifier().key(), key.keyVersionIdentifier().version(),
        key.value(), key.aux(), key.active(), key.type(),
        timestampManager.fromDate(key.createDate()),
        key.updateDate().map(timestampManager::fromDate).orElse(null)
    });
  }

  /**
   * Prepared Statement: delete active keys.
   *
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(KEY_DELETE_ACTIVE_STMT)
  public StatementBinder.Builder<?> deleteActiveKey(final TableConfiguration tableConfiguration) {
    final String baseDelete = """
        delete from %s.%s 
        where owner = ? and key_name = ? and version = ?
        """;
    final String delete = String.format(baseDelete,
        tableConfiguration.keyspace(), tableConfiguration.activeKeysTable());
    return StatementBinder.<KeyVersionIdentifier>builder().with(delete).with((identifier) -> new Object[]{
        identifier.owner(), identifier.key(), identifier.version()
    });
  }

  /**
   * Prepared Statement: delete key version.
   *
   * @param tableConfiguration table configuration.
   * @return statement binder.
   */
  @IntoMap
  @Provides
  @Singleton
  @StringKey(KEY_DELETE_VERSION_STMT)
  public StatementBinder.Builder<?> deleteVersionKey(final TableConfiguration tableConfiguration) {
    final String baseDelete = """
        delete from %s.%s 
        where owner = ? and key_name = ? and version = ?
        """;
    final String delete = String.format(baseDelete,
        tableConfiguration.keyspace(), tableConfiguration.keysTable());
    return StatementBinder.<KeyVersionIdentifier>builder().with(delete).with((identifier) -> new Object[]{
        identifier.owner(), identifier.key(), identifier.version()
    });
  }
}
