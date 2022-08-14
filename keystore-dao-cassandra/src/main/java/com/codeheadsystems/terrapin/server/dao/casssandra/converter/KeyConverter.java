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

package com.codeheadsystems.terrapin.server.dao.casssandra.converter;

import com.codeheadsystems.terrapin.server.dao.casssandra.manager.TimestampManager;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKey;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyConverter.class);

  private final TimestampManager timestampManager;

  /**
   * Default Constructor.
   */
  @Inject
  public KeyConverter(final TimestampManager timestampManager) {
    LOGGER.info("KeyConverter({})", timestampManager);
    this.timestampManager = timestampManager;
  }

  public Key toKey(final Row row) {
    final KeyVersionIdentifier identifier = ImmutableKeyVersionIdentifier.builder()
        .owner(row.getString("owner"))
        .key(row.getString("key_name"))
        .version(row.getLong("version"))
        .build();
    return ImmutableKey.builder()
        .keyVersionIdentifier(identifier)
        .active(row.getBoolean("active"))
        .type(row.getString("type"))
        .value(row.get("value", ExtraTypeCodecs.BLOB_TO_ARRAY))
        .createDate(timestampManager.toDate(row, "create_date")
            .orElseThrow(() -> new IllegalArgumentException("CreateDate is null: " + row.getString("create_date"))))
        .updateDate(timestampManager.toDate(row, "update_date"))
        .build();
  }
}
