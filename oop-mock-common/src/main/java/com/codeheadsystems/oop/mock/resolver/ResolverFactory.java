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
 * <p>
 * Update: 5/30/2022: This is gotten too weird. Prepare for a refactoring.
 * BWA HAHA HAHAHAH AHA HA
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
    return buildForClass(clazz);
  }

  /**
   * For now, this uses a recursive pattern to build the main object. Any object can be created, as long as objects it
   * depends on exists in the map given to us. And that map cannot be named. There must be a better way in dagger to
   * do this.
   */
  private <T> T buildForClass(final Class<?> clazz) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
    final Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
        .filter(c -> c.isAnnotationPresent(Inject.class))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No constructor with @Inject for " + clazz.getCanonicalName()));
    final Object[] args = new Object[constructor.getParameterCount()];
    final Class<?>[] params = constructor.getParameterTypes();
    LOGGER.debug("param count: " + constructor.getParameterCount());
    for (int i = 0; i < args.length; i++) {
      Class<?> param = params[i];
      args[i] = instanceMap.get(param);
      LOGGER.debug("   {} -> {}", param, args[i]);
      if (args[i] == null) {
        // hail mary...
        args[i] = buildForClass(param);
        if (args[i] == null) {
          throw new IllegalArgumentException("Missing injected param for class " + resolverClass + " type " + params[i].getName());
        }
      }
    }
    return (T) constructor.newInstance(args);
  }

}
