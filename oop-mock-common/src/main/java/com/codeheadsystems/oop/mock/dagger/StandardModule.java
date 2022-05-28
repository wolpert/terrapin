// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.dagger;

import static com.codeheadsystems.oop.mock.manager.ResourceLookupManager.LOOKUP_CLASS;

import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.factory.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;

@Module(includes = {StandardModule.BindingsModule.class})
public class StandardModule {

    public static final String OOP_SYSTEM = "OOP_SYSTEM";
    public static final String DEFAULT = "DEFAULT";

    @Provides
    @Singleton
    public Hasher hasher(@Named(OOP_SYSTEM) final Optional<String> system) {
        return new Hasher(system.orElse(DEFAULT));
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper(final ObjectMapperFactory factory) {
        return factory.objectMapper();
    }

    @Module
    interface BindingsModule {

        /**
         * Provides the system namespace to set. If not set, it will be default: DEFAULT
         *
         * @return system name.
         */
        @BindsOptionalOf
        @Named(OOP_SYSTEM)
        String systemName();

        /**
         * Set this if you want to use your own classloader to base the lookup on. Needed if the
         * ResourceLookupManager cannot find the resource you need.
         *
         * @return an instance.
         * @see com.codeheadsystems.oop.mock.manager.ResourceLookupManager
         */
        @BindsOptionalOf
        @Named(LOOKUP_CLASS)
        ClassLoader lookupClassLoader();
    }

}
