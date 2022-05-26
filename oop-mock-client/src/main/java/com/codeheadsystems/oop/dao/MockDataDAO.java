// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dao;

import com.codeheadsystems.oop.mock.model.MockedData;
import com.codeheadsystems.oop.mock.resolver.MockDataResolver;

public interface MockDataDAO extends MockDataResolver {

    void store(String namespace, String lookup, String discriminator, MockedData data);

    void delete(String namespace, String lookup, String discriminator);

}
