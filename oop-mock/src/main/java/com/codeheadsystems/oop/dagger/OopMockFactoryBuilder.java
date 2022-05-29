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

package com.codeheadsystems.oop.dagger;

import static com.codeheadsystems.oop.mock.dagger.ResolverModule.DEFAULT_RESOLVER;

import com.codeheadsystems.oop.OopMockFactory;
import com.codeheadsystems.oop.mock.dagger.StandardModule;
import com.codeheadsystems.oop.mock.resolver.InMemoryResolver;
import com.codeheadsystems.oop.mock.resolver.MockDataResolver;
import com.codeheadsystems.oop.mock.resolver.ResolverFactory;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.lang.reflect.InvocationTargetException;
import javax.inject.Named;
import javax.inject.Singleton;

@Component(modules = {StandardModule.class, OopMockFactoryBuilder.ServerResolverModule.class})
@Singleton
public interface OopMockFactoryBuilder {

    OopMockFactory factory();

    @Module
    class ServerResolverModule {

        @Provides
        @Singleton
        public MockDataResolver resolver(final ResolverFactory factory) {
            try {
                return factory.build();
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * We allow the server to use the inMemory resolver by default, which if nothing is defined will
         * disable oopMock completely.
         *
         * @return InMemoryResolver classname.
         */
        @Named(DEFAULT_RESOLVER)
        @Provides
        @Singleton
        String defaultResolver(){
            return InMemoryResolver.class.getCanonicalName();
        }

    }
}
