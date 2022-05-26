// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.manager;

import java.io.InputStream;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class ResourceLookupManager {

    public static final String LOOKUP_CLASS = "ResourceLookupManager.lookupClass";
    private final ClassLoader lookupClassLoader;

    @Inject
    public ResourceLookupManager(@Named(LOOKUP_CLASS) final Optional<ClassLoader> lookupClassLoader) {
        this.lookupClassLoader = lookupClassLoader.orElse(ResourceLookupManager.class.getClassLoader());
    }

    public Optional<InputStream> inputStream(final String filename) {
        return Optional.ofNullable(lookupClassLoader.getResourceAsStream(filename));
    }

}
