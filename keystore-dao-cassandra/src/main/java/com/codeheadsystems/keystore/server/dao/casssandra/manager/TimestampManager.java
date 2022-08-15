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

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides Cassandra valid timestamps.
 */
@Singleton
public class TimestampManager {

  private final Clock clock;

  /**
   * Default constructor.
   *
   * @param clock to get the timestamp.
   */
  @Inject
  public TimestampManager(final Clock clock) {
    this.clock = clock;
  }

  /**
   * Gets the current timestamp as an instant.
   *
   * @return instant of now.
   */
  public Instant timestamp() {
    return clock.instant();
  }

  public Instant fromDate(final Date date) {
    return date.toInstant();
  }

  /**
   * Converts the column in the row to a date via the zoned date time.
   *
   * @param row from cassandra.
   * @param columnName the column name.
   * @return an optinal date.
   */
  public Optional<Date> toDate(final Row row, final String columnName) {
    final ZonedDateTime zonedDateTime = row.get(columnName, GenericType.ZONED_DATE_TIME);
    if (zonedDateTime != null) {
      return Optional.of(new Date(zonedDateTime.toInstant().toEpochMilli()));
    } else {
      return Optional.empty();
    }
  }
}
