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
import com.codeheadsystems.terrapin.keystore.api.KeyManagerService;
import com.codeheadsystems.terrapin.keystore.converter.ApiConverter;
import com.codeheadsystems.terrapin.keystore.exception.AlreadyExistsException;
import com.codeheadsystems.terrapin.keystore.manager.KeyManager;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class KeyManagerResource implements KeyManagerService, JettyResource {

    private final ApiConverter apiConverter;
    private final KeyManager keyManager;
    public static final Logger LOGGER = LoggerFactory.getLogger(KeyManagerResource.class);

    @Inject
    public KeyManagerResource(final ApiConverter apiConverter,
                              final KeyManager keyManager) {
        LOGGER.info("KeyManagerResource({},{})", apiConverter, keyManager);
        this.apiConverter = apiConverter;
        this.keyManager = keyManager;
    }

    @Override
    public Key create(final String owner, final String keyId) {
        LOGGER.debug("create({},{})", owner, keyId);
        final KeyIdentifier identifier = apiConverter.toDaoKeyIdentifier(owner, keyId);
        try {
            return apiConverter.toApiKey(keyManager.create(identifier));
        } catch (AlreadyExistsException e) {
            throw new WebApplicationException("The key already exists", Response.Status.CONFLICT);
        }
    }

    @Override
    public Response delete(final String owner, final String keyId) {
        LOGGER.debug("delete({},{})", owner, keyId);
        return null;
    }

    @Override
    public Response delete(final String owner, final String keyId, final Long version) {
        LOGGER.debug("delete({},{},{})", owner, keyId, version);
        return null;
    }
}
