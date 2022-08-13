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

package com.codeheadsystems.terrapin.server.dao.casssandra.manager;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides ability to convert an object to the correct prepared statement.
 */
public class StatementBinder<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatementBinder.class);

  private final Function<T, Object[]> binder;
  private final PreparedStatement preparedStatement;
  private final String cqlStatement;
  private final Class<T> type;

  /**
   * Default constructor.
   *
   * @param cqlSession   used for creating the preparer.
   * @param cqlStatement Statement we are preparing.
   * @param binder       execution binder to convert.
   * @param type         to get the type of object. Java generics almost work.
   */
  @AssistedInject
  public StatementBinder(final CqlSession cqlSession,
                         @Assisted final String cqlStatement,
                         @Assisted final Function<T, Object[]> binder,
                         @Assisted final Class<T> type) {
    this.type = type;
    final SimpleStatement statement = SimpleStatement.newInstance(cqlStatement);
    this.preparedStatement = cqlSession.prepare(statement);
    this.binder = binder;
    this.cqlStatement = cqlStatement;
    LOGGER.info("StatementBinder({})", cqlStatement);
  }

  /**
   * Binds the object to an execution instance.
   *
   * @param object to bind.
   * @return bound statement.
   */
  public BoundStatement bind(final T object) {
    LOGGER.debug("bind [{}] to {}", cqlStatement, object.getClass().getSimpleName());
    return preparedStatement.bind(binder.apply(object));
  }

  public Class<T> getType() {
    return type;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ":[" + cqlStatement + "]";
  }
}
