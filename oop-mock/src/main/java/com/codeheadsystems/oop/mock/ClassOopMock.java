/*
 *    Copyright (c) 2022 Ned Wolpert <ned.wolpert@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
        return proxyManager.proxy(namespace, lookup, id, returnClass, supplier);
    }

    @Override
    public String toString() {
        return "ClassOopMock{" +
                "namespace='" + namespace + '\'' +
                '}';
    }
}
