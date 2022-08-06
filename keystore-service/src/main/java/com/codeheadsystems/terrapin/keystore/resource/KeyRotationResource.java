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

package com.codeheadsystems.terrapin.keystore.resource;

import com.codeheadsystems.terrapin.keystore.api.Key;
import com.codeheadsystems.terrapin.keystore.api.KeyRotationService;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class KeyRotationResource implements KeyRotationService, JettyResource {

  public static final Logger LOGGER = LoggerFactory.getLogger(KeyRotationResource.class);

  @Inject
  public KeyRotationResource() {
    LOGGER.info("KeyRotationResource()");
  }

  @Override
  public Key rotate(final String owner, final String keyId) {
    LOGGER.debug("rotate({},{})", owner, keyId);
    return null;
  }
}
