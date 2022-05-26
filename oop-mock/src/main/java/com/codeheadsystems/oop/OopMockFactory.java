// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop;

import com.codeheadsystems.oop.dagger.ClassOopMockFactory;
import com.codeheadsystems.oop.dagger.DaggerOopMockFactoryBuilder;
import com.codeheadsystems.oop.mock.PassThroughOopMock;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory just contains the generator to use. If there is no configuration, then we are in pass-thru mode.
 */
@Singleton
public class OopMockFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(OopMockFactory.class);

    private final Generator generator;

    /**
     * The default getInstance you can use. In most cases, call this method to setup the instance, and inject this
     * into your system. It will use dagger to configure itself based on the default configuration file in your
     * classpath. However, advanced users (who happen to use dagger as well) can just have a provider for the
     * OopMockConfiguration you want to use, and then you can inject the factory as needed.
     *
     * @return instance.
     */
    public static OopMockFactory getInstance() {
        return DaggerOopMockFactoryBuilder.create().factory();
    }

    @Inject
    public OopMockFactory(final OopMockConfiguration oopMockConfiguration,
                          final ClassOopMockFactory classOopMockFactory,
                          final PassThroughOopMock passThroughOopMock) {
        if (oopMockConfiguration.enabled()) {
            LOGGER.info("OopMockFactory() -> enabled");
            final LoadingCache<Class<?>, OopMock> oppMockCache = CacheBuilder.newBuilder()
                    .build(CacheLoader.from(classOopMockFactory::create));
            generator = oppMockCache::getUnchecked;
        } else {
            LOGGER.info("OopMockFactory() -> disabled");
            generator = c -> passThroughOopMock;
        }
    }

    public OopMock generate(final Class<?> clazz) {
        return generator.generate(clazz);
    }

    @FunctionalInterface
    public interface Generator {
        OopMock generate(final Class<?> clazz);
    }
}
