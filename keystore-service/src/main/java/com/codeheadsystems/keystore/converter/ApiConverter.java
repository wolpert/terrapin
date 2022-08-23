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

package com.codeheadsystems.keystore.converter;

import com.codeheadsystems.keystore.api.ImmutableKey;
import com.codeheadsystems.keystore.api.Key;
import com.codeheadsystems.keystore.server.dao.model.ImmutableKeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.ImmutableKeyVersionIdentifier;
import com.codeheadsystems.keystore.server.dao.model.KeyIdentifier;
import com.codeheadsystems.keystore.server.dao.model.KeyVersionIdentifier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ApiConverter {

  public static final String ACTIVE = "active";
  public static final String INACTIVE = "inactive";
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiConverter.class);

  @Inject
  public ApiConverter() {
    LOGGER.info("ApiConverter()");
  }

  public KeyVersionIdentifier toDaoKeyVersionIdentifier(final String owner,
                                                        final String keyId,
                                                        final Long version) {
    return ImmutableKeyVersionIdentifier.builder()
        .owner(owner)
        .key(keyId)
        .version(version)
        .build();
  }

  public KeyIdentifier toDaoKeyIdentifier(final String owner,
                                          final String keyId) {
    return ImmutableKeyIdentifier.builder()
        .owner(owner)
        .key(keyId)
        .build();
  }

  /**
   * The secret stored im the database itself should be encrypted. We do not assume the database encrypts
   * internally, so it should be field-level encryption.
   *
   * @param daoKey
   * @return
   */
  public Key toApiKey(final com.codeheadsystems.keystore.server.dao.model.Key daoKey) {
    final KeyVersionIdentifier identifier = daoKey.keyVersionIdentifier();
    return ImmutableKey.builder()
        .owner(identifier.owner())
        .id(identifier.key())
        .version(identifier.version())
        .key(daoKey.value())
        .status(daoKey.active() ? ACTIVE : INACTIVE)
        .build();
  }
}