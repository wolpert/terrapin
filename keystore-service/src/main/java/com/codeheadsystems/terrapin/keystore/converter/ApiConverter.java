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

package com.codeheadsystems.terrapin.keystore.converter;

import com.codeheadsystems.terrapin.keystore.api.ImmutableKey;
import com.codeheadsystems.terrapin.keystore.api.Key;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ApiConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiConverter.class);

    @Inject
    public ApiConverter() {
        LOGGER.info("ApiConverter()");
    }

    public KeyVersionIdentifier toDaoKeyVersionIdentifier(final com.codeheadsystems.terrapin.keystore.api.Key apiKey) {
        return toDaoKeyVersionIdentifier(apiKey.owner(), apiKey.id(), apiKey.version());
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

    public Key toApiKey(final com.codeheadsystems.terrapin.server.dao.model.Key daoKey) {
        final KeyVersionIdentifier identifier = daoKey.keyVersionIdentifier();
        return ImmutableKey.builder()
                .owner(identifier.owner())
                .id(identifier.key())
                .version(identifier.version())
                .key(daoKey.value())
                .status(daoKey.active() ? "active" : "inactive")
                .build();
    }
}
