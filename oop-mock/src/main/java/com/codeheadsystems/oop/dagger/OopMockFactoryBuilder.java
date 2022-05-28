// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dagger;

import com.codeheadsystems.oop.OopMockFactory;
import com.codeheadsystems.oop.mock.dagger.OopConfigurationModule;
import com.codeheadsystems.oop.mock.dagger.StandardModule;
import dagger.Component;
import javax.inject.Singleton;

@Component(modules = {StandardModule.class})
@Singleton
public interface OopMockFactoryBuilder {

    OopMockFactory factory();
}
