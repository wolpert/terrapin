// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dagger;

import com.codeheadsystems.oop.OopMockFactory;
import dagger.Component;
import javax.inject.Singleton;

@Component(modules = {OopMockFactoryModule.class})
@Singleton
public interface OopMockFactoryBuilder {

    OopMockFactory factory();
}
