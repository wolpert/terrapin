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

package com.codeheadsystems.oop.mock.dagger;

import com.codeheadsystems.oop.OopMockConfiguration;
import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.manager.ResourceLookupManager;
import com.codeheadsystems.oop.mock.translator.Translator;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import javax.inject.Named;

/**
 * Provides all of the base pieces needed for the resolver set.
 * @see com.codeheadsystems.oop.mock.resolver.ResolverFactory
 */
@Module
public interface ResolverModule {

    String RESOLVER = "resolver";

    @Named(RESOLVER) @Binds @IntoMap @ClassKey(OopMockConfiguration.class)
    Object bindsOopMockConfiguration(OopMockConfiguration configuration);

    @Named(RESOLVER) @Binds @IntoMap @ClassKey(JsonConverter.class)
    Object bindsJsonConverter(JsonConverter converter);

    @Named(RESOLVER) @Binds @IntoMap @ClassKey(ResourceLookupManager.class)
    Object bindsResourceLookupManager(ResourceLookupManager manager);

    @Named(RESOLVER) @Binds @IntoMap @ClassKey(Translator.class)
    Object bindsTranslator(Translator translator);

    @Named(RESOLVER) @Binds @IntoMap @ClassKey(Hasher.class)
    Object bindsHasher(Hasher hasher);




}
