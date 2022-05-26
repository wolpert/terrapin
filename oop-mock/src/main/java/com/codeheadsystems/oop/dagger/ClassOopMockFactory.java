// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dagger;

import com.codeheadsystems.oop.mock.ClassOopMock;
import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface ClassOopMockFactory {

    ClassOopMock create(final Class<?> clazz);

}
