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

import com.codeheadsystems.keystore.server.dao.model.ImmutableKeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.ImmutableOwnerIdentifier;
import com.codeheadsystems.keystore.server.dao.model.KeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.OwnerIdentifier;
import com.datastax.oss.driver.api.core.cql.Row;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts owners and other objects.
 */
@Singleton
public class OwnerConverter {

  /**
   * The constant OWNER.
   */
  public static final String OWNER = "owner";
  /**
   * The constant LOOKUP.
   */
  public static final String LOOKUP = "lookup";
  private static final Logger LOGGER = LoggerFactory.getLogger(OwnerConverter.class);

  /**
   * Default constructor.
   */
  @Inject
  public OwnerConverter() {
    LOGGER.info("OwnerConverter()");
  }

  /**
   * Converts a cassandra row to an owner identifier.
   * *
   *
   * @param row from cassandra. Cannot be null;
   * @return OwnerIdentifier. owner identifier
   */
  public OwnerIdentifier toOwnerIdentifier(final Row row) {
    LOGGER.debug("toOwnerIdentifier()");
    return ImmutableOwnerIdentifier.builder().owner(row.getString(OWNER)).build();
  }

  /**
   * Convers the owner row to a key identifier.
   *
   * @param row from cassandra.
   * @return a key identifier.
   */
  public KeyIdentifier toKeyIdentifier(final Row row) {
    return ImmutableKeyIdentifier.builder()
        .owner(row.getString(OWNER))
        .key(row.getString(LOOKUP))
        .build();
  }
}
