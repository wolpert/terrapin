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

import com.codeheadsystems.oop.OopMockConfiguration;
import com.codeheadsystems.oop.ResolverConfiguration;
import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.manager.ResourceLookupManager;
import com.codeheadsystems.oop.mock.translator.Translator;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ResolverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverFactory.class);

    private final Map<Class<?>, Object> instanceMap;
    private final String resolverClass;

    @Inject
    public ResolverFactory(final OopMockConfiguration configuration,
                           final JsonConverter converter,
                           final ResourceLookupManager manager,
                           final Translator translator,
                           final Hasher hasher) {
        LOGGER.info("ResolverFactory({})",configuration);
        final ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();
        builder.put(OopMockConfiguration.class, configuration);
        builder.put(JsonConverter.class, converter);
        builder.put(ResourceLookupManager.class, manager);
        builder.put(Translator.class, translator);
        builder.put(Hasher.class, hasher);
        instanceMap = builder.build();
        resolverClass = configuration.resolverConfiguration()
                .map(ResolverConfiguration::resolverClass)
                .orElseGet(InMemoryResolver.class::getCanonicalName);
    }

    public MockDataResolver build() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        LOGGER.info("build({})", resolverClass);
        final Class<?> clazz = Class.forName(resolverClass);
        if (!MockDataResolver.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Resolver class is not a MockDataResolver: " + resolverClass);
        }
        final Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No constructor with @Inject for " + resolverClass));
        final Object[] args = new Object[constructor.getParameterCount()];
        final Class<?>[] params = constructor.getParameterTypes();
        LOGGER.debug("param count: " + constructor.getParameterCount());
        for (int i = 0; i < args.length; i++) { // First one is the class itself, if the number is > 0
            Class<?> param = params[i];
            args[i] = instanceMap.get(param);
            LOGGER.debug("   {} -> {}", param, args[i]);
            if (args[i] == null) {
                throw new IllegalArgumentException("Missing injected param for class " + resolverClass + " type " + params[i].getName());
            }
        }
        return (MockDataResolver) constructor.newInstance(args);
    }

}
