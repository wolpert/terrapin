// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Hasher {

    public static final String NAMESPACE_DELIMINATOR = ":";
    public static final String DELIMINATOR = ".";
    public static final String OOP_SYSTEM = "OOP_SYSTEM";
    public final String system;

    /**
     * Used to get the current system.
     *
     * @param system
     */
    @Inject
    public Hasher(String system) {
        this.system = system;
    }

    public String hash(final String... args) {
        return String.join(DELIMINATOR, args);
    }

    /**
     * Used to get the namespace for this instance.
     *
     * @param clazz for the namespace.
     * @return String
     */
    public String namespace(final Class<?> clazz) {
        return String.join(NAMESPACE_DELIMINATOR, system, clazz.getCanonicalName());
    }

}
