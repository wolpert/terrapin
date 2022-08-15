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

package com.codeheadsystems.keystore.server.dao.casssandra.manager;

import com.codeheadsystems.keystore.server.dao.casssandra.dagger.StatementBinderFactory;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
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

  /**
   * Default constructor.
   *
   * @param cqlSession   used for creating the preparer.
   * @param cqlStatement Statement we are preparing.
   * @param binder       execution binder to convert.
   */
  public StatementBinder(final CqlSession cqlSession,
                         final String cqlStatement,
                         final Function<T, Object[]> binder) {
    LOGGER.info("StatementBinder({})", cqlStatement);
    final SimpleStatement statement = SimpleStatement.newInstance(cqlStatement);
    this.preparedStatement = cqlSession.prepare(statement);
    this.binder = binder;
    this.cqlStatement = cqlStatement;
  }

  /**
   * Given the builder provides the statement binder.
   *
   * @param cqlSession to use.
   * @param builder the builder we have.
   */
  public StatementBinder(final CqlSession cqlSession,
                         final Builder<T> builder) {
    this(cqlSession, builder.cqlStatement, builder.binder);
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /**
   * Binds the object to an execution instance.
   *
   * @param object to bind.
   * @return bound statement.
   */
  public BoundStatement bind(final T object) {
    LOGGER.debug("bind [{}] to {}", cqlStatement,
        (object != null ? object.getClass().getSimpleName() : null));
    return preparedStatement.bind(binder.apply(object));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ":[" + cqlStatement + "]";
  }

  /**
   * A builder for the statement binder.
   *
   * @param <T> the type of object this takes.
   */
  public static class Builder<T> {
    private String cqlStatement;
    private Function<T, Object[]> binder;

    protected Builder() {

    }

    public Builder<T> with(final String cqlStatement) {
      this.cqlStatement = cqlStatement;
      return this;
    }

    public Builder<T> with(final Function<T, Object[]> binder) {
      this.binder = binder;
      return this;
    }

    public StatementBinder<T> build(final StatementBinderFactory factory) {
      return factory.build(cqlStatement, binder);
    }
  }
}
