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
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.inject.Singleton;

@Module
public class StatementModule {

  public static final String DETAILS = "details";
  public static final String STORE_OWNER_STMT = "OWNER STORE";
  public static final String LOAD_OWNER_STMT = "LOAD OWNER";

  @IntoMap
  @Provides
  @Singleton
  @StringKey(STORE_OWNER_STMT)
  public StatementBinder ownerStore(final StatementBinderFactory factory,
                                    final TimestampManager timestampManager,
                                    final TableConfiguration tableConfiguration) {
    final String baseInsert = "insert into %s.%s (owner, lookup, create_date) values (?,'%s',?)";
    final String insert = String.format(baseInsert,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable(), DETAILS);
    return factory.build(insert, (owner) -> new Object[]{owner, timestampManager.timestamp()});
  }

  @IntoMap
  @Provides
  @Singleton
  @StringKey(LOAD_OWNER_STMT)
  public StatementBinder ownerLoad(final StatementBinderFactory factory,
                                   final TableConfiguration tableConfiguration) {
    final String baseSelect = "select * from %s.%s where owner = ? and lookup = '%s'";
    final String select = String.format(baseSelect,
        tableConfiguration.keyspace(), tableConfiguration.ownersTable(), DETAILS);
    return factory.build(select, (owner) -> new Object[]{owner});
  }

}
