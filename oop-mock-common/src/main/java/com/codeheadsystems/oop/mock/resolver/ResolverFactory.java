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

package com.codeheadsystems.oop.mock.resolver;

import static com.codeheadsystems.oop.mock.dagger.ResolverModule.RESOLVER_CLASSNAME;
import static com.codeheadsystems.oop.mock.dagger.ResolverModule.RESOLVER_MAP;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a builder for the resolver as defined in the OppMockConfiguration file. Note that it's generic for
 * also the DAO instance as well. We use this because we want the configuration file to define the resolver we
 * need to include. So this one class needs runtime injection. 
 */
@Singleton
public class ResolverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverFactory.class);

    private final Map<Class<?>, Object> instanceMap;
    private final String resolverClass;

    @Inject
    public ResolverFactory(@Named(RESOLVER_CLASSNAME) final String resolverClass,
                           @Named(RESOLVER_MAP) final Map<Class<?>, Object> instanceMap) {
        LOGGER.info("ResolverFactory({})", resolverClass);
        this.instanceMap = instanceMap;
        this.resolverClass = resolverClass;
    }

    public <T> T build() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        LOGGER.info("build({})", resolverClass);
        final Class<?> clazz = Class.forName(resolverClass);
        final Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No constructor with @Inject for " + resolverClass));
        final Object[] args = new Object[constructor.getParameterCount()];
        final Class<?>[] params = constructor.getParameterTypes();
        LOGGER.debug("param count: " + constructor.getParameterCount());
        for (int i = 0; i < args.length; i++) {
            Class<?> param = params[i];
            args[i] = instanceMap.get(param);
            LOGGER.debug("   {} -> {}", param, args[i]);
            if (args[i] == null) {
                throw new IllegalArgumentException("Missing injected param for class " + resolverClass + " type " + params[i].getName());
            }
        }
        return (T) constructor.newInstance(args);
    }

}
