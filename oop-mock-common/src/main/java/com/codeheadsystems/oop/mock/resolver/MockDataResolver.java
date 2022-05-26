// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.resolver;

import com.codeheadsystems.oop.mock.model.MockedData;
import java.util.Optional;

/**
 * Provides the way we want to lookup the data in the data store for the discriminator.
 * This should be the full discriminator including namespace. These, obviously, need to
 * be unique.
 */
public interface MockDataResolver {

    Optional<MockedData> resolve(String namespace, String lookup, String discriminator);

}
