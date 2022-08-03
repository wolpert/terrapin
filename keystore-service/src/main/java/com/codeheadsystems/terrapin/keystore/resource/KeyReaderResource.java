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
import com.codeheadsystems.terrapin.keystore.api.KeyReaderService;
import com.codeheadsystems.terrapin.keystore.converter.ApiConverter;
import com.codeheadsystems.terrapin.keystore.manager.KeyStoreReaderManager;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class KeyReaderResource implements KeyReaderService, JettyResource {

    public static final Logger LOGGER = LoggerFactory.getLogger(KeyReaderResource.class);
    private final ApiConverter apiConverter;
    private final KeyStoreReaderManager keyStoreReaderManager;

    @Inject
    public KeyReaderResource(final ApiConverter apiConverter,
                             final KeyStoreReaderManager keyStoreReaderManager) {
        LOGGER.info("KeyReaderResource({},{})", apiConverter, keyStoreReaderManager);
        this.apiConverter = apiConverter;
        this.keyStoreReaderManager = keyStoreReaderManager;
    }

    @Override
    public Key get(final String owner, final String keyId) {
        LOGGER.debug("get({},{})", owner, keyId);
        final KeyIdentifier identifier = apiConverter.toDaoKeyIdentifier(owner, keyId);
        return keyStoreReaderManager.getKey(identifier)
                .map(apiConverter::toApiKey)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @Override
    public Key get(final String owner, final String keyId, final Long version) {
        LOGGER.debug("get({},{},{})", owner, keyId, version);
        final KeyVersionIdentifier identifier = apiConverter.toDaoKeyVersionIdentifier(owner, keyId, version);
        return keyStoreReaderManager.getKey(identifier)
                .map(apiConverter::toApiKey)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }
}
