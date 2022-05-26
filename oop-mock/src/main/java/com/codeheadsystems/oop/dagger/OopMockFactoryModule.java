// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dagger;

import com.codeheadsystems.oop.ImmutableOopMockConfiguration;
import com.codeheadsystems.oop.OopMockConfiguration;
import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.dagger.StandardModule;
import com.codeheadsystems.oop.mock.manager.InMemoryDataStoreManager;
import com.codeheadsystems.oop.mock.manager.ResourceLookupManager;
import com.codeheadsystems.oop.mock.model.InMemoryMockedDataStore;
import com.codeheadsystems.oop.mock.resolver.InMemoryResolver;
import com.codeheadsystems.oop.mock.resolver.MockDataResolver;
import com.codeheadsystems.oop.mock.translator.JsonTranslator;
import com.codeheadsystems.oop.mock.translator.Translator;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: get all this from the configuration
 */
@Module(includes = {StandardModule.class})
public class OopMockFactoryModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(OopMockFactoryModule.class);
    public static final String CONFIGURATION_FILENAME = "oopMockConfiguration.json";

    private final String configurationFileName;
    private final OopMockConfiguration configuration;

    public OopMockFactoryModule() {
        this(CONFIGURATION_FILENAME);
    }

    public OopMockFactoryModule(final String configurationName){
        this.configurationFileName = configurationName;
        configuration=null;
    }

    public OopMockFactoryModule(final OopMockConfiguration configuration){
        this.configuration = configuration;
        this.configurationFileName = null;
    }

    @Provides
    @Singleton
    public Translator translator(final JsonTranslator translator) {
        return translator;
    }

    /**
     * TODO: use configuration to generate. Right now it assumes in memory, which is bunk.
     *
     * @param resolver
     * @return
     */
    @Provides
    @Singleton
    public MockDataResolver resolver(final InMemoryResolver resolver) {
        return resolver;
    }

    // TODO: Remove this when you fix the resolver.
    @Provides
    @Singleton
    public InMemoryMockedDataStore inMemoryMockedDataStore(final InMemoryDataStoreManager inMemoryDataStoreManager,
                                                           final OopMockConfiguration configuration) {
        return inMemoryDataStoreManager.from(configuration.mockDataFileName().get()); // yeah, it'll blow up.
    }

    @Provides
    @Singleton
    public OopMockConfiguration configuration(final ResourceLookupManager manager,
                                              final JsonConverter converter) {
        if (configuration!=null){
            return configuration;
        }
        return manager.inputStream(configurationFileName)
                .map(is -> converter.convert(is, OopMockConfiguration.class))
                .orElseGet(this::defaultOopMockConfiguration);
    }

    private OopMockConfiguration defaultOopMockConfiguration() {
        LOGGER.warn("No configuration found, using default disabled configuration");
        return ImmutableOopMockConfiguration.builder().build();
    }
}
