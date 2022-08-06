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

package com.codeheadsystems.oop;

import com.codeheadsystems.oop.dagger.ClassOopMockFactory;
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
