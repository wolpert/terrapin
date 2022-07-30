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

package com.codeheadsystems.terrapin.keystore.manager;

import static com.codeheadsystems.terrapin.keystore.module.RNGModule.PROVIDED_RNG;

import com.codeheadsystems.terrapin.common.helper.DataHelper;
import com.codeheadsystems.terrapin.keystore.exception.AlreadyExistsException;
import com.codeheadsystems.terrapin.keystore.model.RNG;
import com.codeheadsystems.terrapin.server.dao.KeyDAO;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKey;
import com.codeheadsystems.terrapin.server.dao.model.ImmutableKeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import java.util.Date;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class KeyManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyManager.class);
    public static final int KEY_SIZE = 32;

    private final KeyDAO keyDAO;
    private final RNG rng;
    private final DataHelper dataHelper;

    @Inject
    public KeyManager(final KeyDAO keyDAO,
                      @Named(PROVIDED_RNG) final RNG rng,
                      final DataHelper dataHelper) {
        LOGGER.info("KeyManager({},{},{})", keyDAO, rng, dataHelper);
        this.keyDAO = keyDAO;
        this.rng = rng;
        this.dataHelper = dataHelper;
    }

    public Optional<Key> getKey(final KeyVersionIdentifier identifier) {
        LOGGER.debug("getKey({})", identifier);
        return keyDAO.load(identifier);
    }

    public Optional<Key> getKey(final KeyIdentifier identifier) {
        LOGGER.debug("getKey({})", identifier);
        return keyDAO.load(identifier);
    }

    public Key create(final KeyIdentifier identifier) throws AlreadyExistsException {
        LOGGER.debug("create({})", identifier);
        final Optional<Key> currentKey = keyDAO.load(identifier);
        if (currentKey.isPresent()) {
            throw new AlreadyExistsException();
        }
        final KeyVersionIdentifier newKeyIdentifier = ImmutableKeyVersionIdentifier.builder()
                .owner(identifier.owner()).key(identifier.key()).version(1L).build();
        final byte[] secret = new byte[KEY_SIZE];
        rng.random(secret);
        final Key key = ImmutableKey.builder()
                .keyVersionIdentifier(newKeyIdentifier)
                .type("256")
                .active(true)
                .createDate(new Date())
                .value(secret)
                .build();
        keyDAO.store(key);
        dataHelper.clear(secret); // secret is copied to make the key.
        return key;
    }
}
