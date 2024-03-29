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
import com.datastax.oss.driver.api.core.cql.Statement;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages bound statements.
 */
@Singleton
public class BoundStatementManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(BoundStatementManager.class);
  private final Map<String, StatementBinder<?>> preparedStatementMap;

  /**
   * Default Constructor.
   *
   * @param preparedStatementMap for binding.
   * @param factory              the factory
   */
  @Inject
  public BoundStatementManager(final Map<String, StatementBinder.Builder<?>> preparedStatementMap,
                               final StatementBinderFactory factory) {
    LOGGER.info("BoundStatementManager({})", preparedStatementMap);
    this.preparedStatementMap = preparedStatementMap.entrySet().stream()
        .map(e -> Map.entry(e.getKey(), factory.build(e.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    LOGGER.info("BoundStatementManager prepared");
  }

  /**
   * Binds the object to the prepared statement for execution.
   *
   * @param <T>                    the type parameter
   * @param statementMapIdentifier for the lookup.
   * @param object                 to bind.
   * @return a statement that can be executed.
   */
  public <T> Statement<?> bind(@Nonnull final String statementMapIdentifier,
                               final T object) {
    final StatementBinder<?> statementBinder = preparedStatementMap.get(statementMapIdentifier);
    if (statementBinder == null) {
      throw new IllegalArgumentException("No such statement binder: " + statementMapIdentifier);
    }
    return ((StatementBinder<T>) statementBinder).bind(object);
  }

}
