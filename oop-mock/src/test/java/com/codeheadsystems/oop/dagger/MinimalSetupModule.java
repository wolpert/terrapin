// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dagger;

import com.codeheadsystems.oop.OopMockFactory;
import com.codeheadsystems.oop.mock.dagger.StandardModule;
import com.codeheadsystems.oop.mock.resolver.InMemoryResolver;
import com.codeheadsystems.oop.mock.resolver.MockDataResolver;
import com.codeheadsystems.oop.mock.translator.JsonTranslator;
import com.codeheadsystems.oop.mock.translator.Translator;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(includes = {StandardModule.class, InMemoryModule.class})
public class MinimalSetupModule {

    @Provides
    @Singleton
    public Translator translator(final JsonTranslator translator) {
        return translator;
    }

    @Provides
    @Singleton
    public MockDataResolver resolver(final InMemoryResolver resolver) {
        return resolver;
    }

    @Component(modules = {MinimalSetupModule.class})
    @Singleton
    public interface FactoryBuilder {
        OopMockFactory create();
    }
}
