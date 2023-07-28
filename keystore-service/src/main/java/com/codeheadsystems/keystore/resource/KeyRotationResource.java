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

package com.codeheadsystems.keystore.resource;

import com.codeheadsystems.keystore.api.Key;
import com.codeheadsystems.keystore.api.KeyRotationService;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rotates the key.
 */
@Singleton
public class KeyRotationResource implements KeyRotationService, JettyResource {

  /**
   * The constant LOGGER.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(KeyRotationResource.class);

  /**
   * Default constructor.
   */
  @Inject
  public KeyRotationResource() {
    LOGGER.info("KeyRotationResource()");
  }

  /**
   * Rotates the key, creating a new key version. Does not deactivate old keys.
   *
   * @param owner of the key.
   * @param keyId that needs rotating.
   * @return the new key.
   */
  @Override
  public Key rotate(final String owner, final String keyId) {
    LOGGER.debug("rotate({},{})", owner, keyId);
    return null;
  }
}
