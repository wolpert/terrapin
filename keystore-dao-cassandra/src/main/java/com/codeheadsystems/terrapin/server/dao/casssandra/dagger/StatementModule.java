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

package com.codeheadsystems.terrapin.server.dao.casssandra.dagger;

import com.codeheadsystems.terrapin.server.dao.casssandra.configuration.TableConfiguration;
import com.codeheadsystems.terrapin.server.dao.casssandra.manager.StatementBinder;
import com.codeheadsystems.terrapin.server.dao.casssandra.manager.TimestampManager;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class StatementModule {

  public static final String DETAILS = "details";
  public static final String STORE_OWNER_STMT = "owner.store";
  public static final String STORE_OWNER_KEY_STMT = "owner.store.key";
  public static final String LOAD_OWNER_STMT = "owner.load";
  public static final String LOAD_OWNER_KEY_STMT = "owner.load.key";
  public static final String STORE_KEY_STMT = "key.store";
  public static final String STORE_ACTIVE_KEY_STMT = "key.store.active";
  public static final String DELETE_ACTIVE_KEY_STMT = "key.delete.active";

  @IntoMap
  @Provides
  @Singleton
  @StringKey(STORE_OWNER_STMT)
  public StatementBinder<?> ownerStore(final StatementBinderFactory factory,
                                       final TimestampManager timestampManager,
                                       final TableConfiguration tableConfiguration) {
    final String baseInsert = "insert into %s.%s (owner, lookup, create_date) values (?,'%s',?)";
    final String insert = String.format(baseInsert,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable(), DETAILS);
    return factory.build(insert, String.class, (owner) -> new Object[]{owner, timestampManager.timestamp()});
  }

  @IntoMap
  @Provides
  @Singleton
  @StringKey(STORE_OWNER_KEY_STMT)
  public StatementBinder<?> ownerStoreKey(final StatementBinderFactory factory,
                                          final TimestampManager timestampManager,
                                          final TableConfiguration tableConfiguration) {
    final String baseInsert = "insert into %s.%s (owner, lookup, create_date) values (?,?,?)";
    final String insert = String.format(baseInsert,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable(), DETAILS);
    return factory.build(insert, Key.class, (key) -> new Object[]{
        key.keyVersionIdentifier().owner(), key.keyVersionIdentifier().key(), timestampManager.timestamp()});
  }

  @IntoMap
  @Provides
  @Singleton
  @StringKey(LOAD_OWNER_STMT)
  public StatementBinder<?> ownerLoad(final StatementBinderFactory factory,
                                      final TableConfiguration tableConfiguration) {
    final String baseSelect = "select * from %s.%s where owner = ? and lookup = '%s'";
    final String select = String.format(baseSelect,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable(), DETAILS);
    return factory.build(select, String.class, (owner) -> new Object[]{owner});
  }

  @IntoMap
  @Provides
  @Singleton
  @StringKey(LOAD_OWNER_KEY_STMT)
  public StatementBinder<?> ownerLoadKey(final StatementBinderFactory factory,
                                         final TableConfiguration tableConfiguration) {
    final String baseSelect = "select * from %s.%s where owner = ? and lookup = '%s'";
    final String select = String.format(baseSelect,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable(), DETAILS);
    return factory.build(select, KeyIdentifier.class,
        (identifier) -> new Object[]{identifier.owner(), identifier.key()});
  }

  @IntoMap
  @Provides
  @Singleton
  @StringKey(STORE_KEY_STMT)
  public StatementBinder<?> storeKey(final StatementBinderFactory factory,
                                     final TimestampManager timestampManager,
                                     final TableConfiguration tableConfiguration) {
    final String baseInsert = """
        insert into %s.%s 
          (owner, key_name, version, value, active, type, create_date, update_date)
          values (?,?,?,?,?,?,?,?)""";
    final String insert = String.format(baseInsert,
        tableConfiguration.keyspace(), tableConfiguration.keysTable());
    return factory.build(insert, Key.class, (key) -> new Object[]{
        key.keyVersionIdentifier().owner(), key.keyVersionIdentifier().key(), key.keyVersionIdentifier().version(),
        key.value(), key.active(), key.type(),
        timestampManager.fromDate(key.createDate()),
        key.updateDate().map(timestampManager::fromDate).orElse(null)
    });
  }

  @IntoMap
  @Provides
  @Singleton
  @StringKey(STORE_ACTIVE_KEY_STMT)
  public StatementBinder<?> storeActiveKey(final StatementBinderFactory factory,
                                           final TimestampManager timestampManager,
                                           final TableConfiguration tableConfiguration) {
    final String baseInsert = """
        insert into %s.%s 
          (owner, key_name, version, value, active, type, create_date, update_date)
          values (?,?,?,?,?,?,?,?)""";
    final String insert = String.format(baseInsert,
        tableConfiguration.keyspace(), tableConfiguration.activeKeysTable());
    return factory.build(insert, Key.class, (key) -> new Object[]{
        key.keyVersionIdentifier().owner(), key.keyVersionIdentifier().key(), key.keyVersionIdentifier().version(),
        key.value(), key.active(), key.type(),
        timestampManager.fromDate(key.createDate()),
        key.updateDate().map(timestampManager::fromDate).orElse(null)
    });
  }

  @IntoMap
  @Provides
  @Singleton
  @StringKey(DELETE_ACTIVE_KEY_STMT)
  public StatementBinder<?> deleteActiveKey(final StatementBinderFactory factory,
                                            final TableConfiguration tableConfiguration) {
    final String baseDelete = """
        delete from %s.%s 
        where owner = ? and key_name = ? and version = ?
        """;
    final String delete = String.format(baseDelete,
        tableConfiguration.keyspace(), tableConfiguration.activeKeysTable());
    return factory.build(delete, Key.class, (key) -> new Object[]{
        key.keyVersionIdentifier().owner(), key.keyVersionIdentifier().key(), key.keyVersionIdentifier().version()
    });
  }
}
