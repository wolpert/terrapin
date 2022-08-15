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

package com.codeheadsystems.keystore.server.dao.casssandra.converter;

import com.codeheadsystems.keystore.server.dao.casssandra.manager.TimestampManager;
import com.codeheadsystems.keystore.server.dao.model.ImmutableKey;
import com.codeheadsystems.keystore.server.dao.model.ImmutableKeyVersionIdentifier;
import com.codeheadsystems.keystore.server.dao.model.Key;
import com.codeheadsystems.keystore.server.dao.model.KeyVersionIdentifier;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.type.codec.ExtraTypeCodecs;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts keys to various objects.
 */
@Singleton
public class KeyConverter {

  public static final String CREATE_DATE = "create_date";
  public static final String UPDATE_DATE = "update_date";
  public static final String TYPE = "type";
  public static final String ACTIVE = "active";
  public static final String OWNER = "owner";
  public static final String KEY_NAME = "key_name";
  public static final String VERSION = "version";
  private static final Logger LOGGER = LoggerFactory.getLogger(KeyConverter.class);
  private final TimestampManager timestampManager;

  /**
   * Default Constructor.
   *
   * @param timestampManager to manage timestamps.
   */
  @Inject
  public KeyConverter(final TimestampManager timestampManager) {
    LOGGER.info("KeyConverter({})", timestampManager);
    this.timestampManager = timestampManager;
  }

  /**
   * Convers a row in a result set to a key.
   *
   * @param row from cassandra.
   * @return the resulting key.
   */
  public Key toKey(final Row row) {
    final KeyVersionIdentifier identifier = toKeyVersionIdentifier(row);
    return ImmutableKey.builder()
        .keyVersionIdentifier(identifier)
        .active(row.getBoolean(ACTIVE))
        .type(row.getString(TYPE))
        .value(row.get("value", ExtraTypeCodecs.BLOB_TO_ARRAY))
        .createDate(timestampManager.toDate(row, CREATE_DATE)
            .orElseThrow(() -> new IllegalArgumentException("CreateDate is null: " + row.getString(CREATE_DATE))))
        .updateDate(timestampManager.toDate(row, UPDATE_DATE))
        .build();
  }

  /**
   * Provides a key version identifier for a cassandra row.
   *
   * @param row from a result set.
   * @return usable key version identifier.
   */
  public KeyVersionIdentifier toKeyVersionIdentifier(final Row row) {
    return ImmutableKeyVersionIdentifier.builder()
        .owner(row.getString(OWNER))
        .key(row.getString(KEY_NAME))
        .version(row.getLong(VERSION))
        .build();
  }
}
