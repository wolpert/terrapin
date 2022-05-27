// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock;

import com.codeheadsystems.oop.OopMock;
import com.codeheadsystems.oop.manager.ProxyManager;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This ops mock instance uses the class for a namespace.
 */
public class ClassOopMock implements OopMock {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassOopMock.class);
    private final String namespace;
    private final ProxyManager proxyManager;

    @AssistedInject
    public ClassOopMock(@Assisted final Class<?> clazz,
                        final Hasher hasher,
                        final ProxyManager proxyManager) {
        this.proxyManager = proxyManager;
        this.namespace = hasher.namespace(clazz);
        LOGGER.info("ClassOpsMock({})", clazz);
    }

    @Override
    public <R> R proxy(final Class<R> returnClass,
                       final Supplier<R> supplier,
                       final String lookup,
                       final String id) {
        // TODO There needs to be a live disablement check. So you can disable in real time.
        return proxyManager.proxy(namespace, lookup, id, returnClass, supplier);
    }

    @Override
    public String toString() {
        return "ClassOopMock{" +
                "namespace='" + namespace + '\'' +
                '}';
    }
}