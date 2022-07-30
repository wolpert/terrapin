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

package com.codeheadsystems.terrapin.service.resource;

import com.codeheadsystems.terrapin.keystore.api.Key;
import com.codeheadsystems.terrapin.keystore.api.KeyReaderService;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class KeyReaderResource implements KeyReaderService, KeyStoreResource {

    public static final Logger LOGGER = LoggerFactory.getLogger(KeyReaderResource.class);

    @Inject
    public KeyReaderResource() {
        LOGGER.info("KeyReaderResource()");
    }

    @Override
    public Key get(final String keyId) {
        LOGGER.debug("get({})", keyId);
        LOGGER.info("get({})", keyId);
        return null;
    }

    @Override
    public Key get(final String keyId, final Long version) {
        LOGGER.debug("get({},{})", keyId, version);
        return null;
    }
}
