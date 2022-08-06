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

package com.codeheadsystems.oop.client.dagger;

import com.codeheadsystems.oop.client.OopMockClientFactory;
import com.codeheadsystems.oop.client.dao.MockDataDAO;
import com.codeheadsystems.oop.mock.dagger.ResolverModule;
import com.codeheadsystems.oop.mock.dagger.StandardModule;
import com.codeheadsystems.oop.mock.resolver.ResolverFactory;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.inject.Singleton;

@Component(modules = {StandardModule.class, OopMockClientFactoryBuilder.ClientModule.class})
@Singleton
public interface OopMockClientFactoryBuilder {

  static OopMockClientFactory generate() {
    return DaggerOopMockClientFactoryBuilder.create().factory();
  }

  static OopMockClientFactory generate(final Map<Class<?>, Object> resolverDeps) {
    return DaggerOopMockClientFactoryBuilder.builder()
        .resolverConfigModule(new ResolverModule.ResolverConfigModule(resolverDeps))
        .build().factory();
  }

  OopMockClientFactory factory();

  @Module
  class ClientModule {

    @Provides
    @Singleton
    MockDataDAO dao(final ResolverFactory resolverFactory) {
      try {
        return resolverFactory.build();
      } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
               IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

  }

}
