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

import com.codeheadsystems.terrapin.server.dao.model.Batch;
import com.codeheadsystems.terrapin.server.dao.model.Key;
import com.codeheadsystems.terrapin.server.dao.model.KeyIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.KeyVersionIdentifier;
import com.codeheadsystems.terrapin.server.dao.model.OwnerIdentifier;
import java.util.Optional;

/**
 * The DAO is largely mechanical; no business logic exists here.
 */
public interface KeyDAO {

    void store(Key key);

    Optional<Key> load(KeyVersionIdentifier identifier);

    /**
     * Gets the latest key version for the list of keys.
     */
    Optional<Key> load(KeyIdentifier identifier);

    Optional<OwnerIdentifier> loadOwner(String ownerName);

    Batch<OwnerIdentifier> listOwners();

    Batch<KeyIdentifier> listKeys(OwnerIdentifier identifier);

    Batch<KeyVersionIdentifier> listVersions(KeyIdentifier identifier);

    // These exist for completeness, but need caution when using.

    /**
     * Deletes a specific key version.
     * @param identifier
     *
     * @return boolean if anything was deleted.
     */
    boolean delete(KeyVersionIdentifier identifier);

    /**
     * Deletes all versions for a key
     * @param identifier
     *
     * @return boolean if anything was deleted.
     */
    boolean delete(KeyIdentifier identifier);

    /**
     * Deletes everything tied to the owner.
     * This is surprisingly controversial. We may need to batch this up in a manager if the list of
     * keys is too long.
     *
     * @param identifier
     * @return boolean if anything was deleted.
     */
    boolean delete(OwnerIdentifier identifier);
}
