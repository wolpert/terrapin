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

import com.codeheadsystems.keystore.server.dao.casssandra.manager.StatementBinder;
import com.datastax.oss.driver.api.core.CqlSession;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory from Dagger to create PreparedStatementManagers.
 * This is supposed to be from dagger, but daggers assisted factory does not work with generic types.
 */
@Singleton
public class StatementBinderFactory {

  private final CqlSession cqlSession;

  /**
   * Default constructor.
   *
   * @param cqlSession for binding.
   */
  @Inject
  public StatementBinderFactory(final CqlSession cqlSession) {
    this.cqlSession = cqlSession;
  }

  /**
   * Builds the statement binder.
   *
   * @param builder to use.
   * @param <T> type for the binder.
   * @return a binder.
   */
  public <T> StatementBinder<T> build(final StatementBinder.Builder<T> builder) {
    return new StatementBinder<>(cqlSession, builder);
  }

}
