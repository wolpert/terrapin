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

package com.codeheadsystems.terrapin.server.dao;

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.terrapin.server.dao.casssandra.accessor.CassandraAccessor;
import com.codeheadsystems.terrapin.server.dao.model.Batch;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.OwnerIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.Token;
import io.micrometer.core.instrument.Timer;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CassandraKeyDAO implements KeyDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraKeyDAO.class);

    public static final String OWNER = "owner";
    public static final String PREFIX = "ddbdao.";
    public static final int MAX_TIMES_KEY_STORE = 5;
    private final CassandraAccessor cassandraAccessor;
    private final Metrics metrics;

    @Inject
    public CassandraKeyDAO(final CassandraAccessor cassandraAccessor,
                           final Metrics metrics) {
        LOGGER.info("CassandraKeyDAO({},{})", cassandraAccessor, metrics);
        this.cassandraAccessor = cassandraAccessor;
        this.metrics = metrics;
    }

    private <T> T time(final String methodName,
                       final String owner,
                       final Supplier<T> supplier) {
        final String name = PREFIX + methodName;
        final Timer timer = metrics.registry().timer(name, OWNER, (owner == null ? "null" : owner)); // TODO: Vet cardinality. Set by configuration?
        return metrics.time(name, timer, supplier);
    }

    @Override
    public void store(final Key key) {
        LOGGER.debug("store({})", key.keyVersionIdentifier());
        time("storeKey", key.keyVersionIdentifier().owner(), () -> {
            return null;
        });
    }

    @Override
    public OwnerIdentifier storeOwner(final String owner) {
        LOGGER.debug("storeOwner({})", owner);
        return time("storeOwner", owner, () -> {
            return null;
        });
    }

    @Override
    public Optional<Key> load(final KeyVersionIdentifier identifier) {
        LOGGER.debug("load({})", identifier);
        return time("loadKeyVersion", identifier.owner(), () -> {
            return null;
        });
    }

    /**
     * Query against the active hash, returning the key with the greatest number.
     * Empty optional if there is no active key or if there is no keys in general.
     */
    @Override
    public Optional<Key> load(final KeyIdentifier identifier) {
        LOGGER.debug("load({})", identifier);
        return time("loadKey", identifier.owner(), () -> {
            return null;
        });
    }

    @Override
    public Optional<OwnerIdentifier> loadOwner(final String ownerName) {
        LOGGER.debug("loadOwner({})", ownerName);
        return time("loadOwner", ownerName, () -> {
            return null;
        });
    }

    @Override
    public Batch<OwnerIdentifier> listOwners(final Token nextToken) {
        LOGGER.debug("listOwners()");
        return time("listOwners", null, () -> {
            return null;
        });
    }


    @Override
    public Batch<KeyIdentifier> listKeys(final OwnerIdentifier identifier,
                                         final Token nextToken) {
        LOGGER.debug("listKeys({})", identifier);
        return time("listKeys", identifier.owner(), () -> {
            return null;
        });
    }

    @Override
    public Batch<KeyVersionIdentifier> listVersions(final KeyIdentifier identifier,
                                                    final Token nextToken) {
        LOGGER.debug("listVersions({})", identifier);
        return time("listVersions", identifier.owner(), () -> {
            return null;
        });
    }

    @Override
    public boolean delete(final KeyVersionIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return time("deleteVersions", identifier.owner(), () -> {
            return false;
        });
    }

    @Override
    public boolean delete(final KeyIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return time("deleteKey", identifier.owner(), () -> {
            return false;
        });
    }

    @Override
    public boolean delete(final OwnerIdentifier identifier) {
        LOGGER.debug("delete({})", identifier);
        return time("deleteOwner", identifier.owner(), () -> {
            return false;
        });
    }
}
