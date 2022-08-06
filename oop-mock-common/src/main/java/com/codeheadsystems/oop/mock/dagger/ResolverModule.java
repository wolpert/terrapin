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
import com.codeheadsystems.oop.ResolverConfiguration;
import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.manager.ResourceLookupManager;
import com.codeheadsystems.oop.mock.translator.Translator;
import com.google.common.collect.ImmutableMap;
import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Provides all of the base pieces needed for the resolver set.
 *
 * @see com.codeheadsystems.oop.mock.resolver.ResolverFactory
 */
@Module(includes = {ResolverModule.ResolverConfigModule.class})
public interface ResolverModule {

  String RESOLVER_MAP = "resolver_map";
  String RESOLVER_INTERNAL_MAP = "resolver internal map";
  String DEFAULT_RESOLVER = "DEFAULT RESOLVER";
  String RESOLVER_CLASSNAME = "RESOLVER CLASSNAME";
  String RESOLVER_ADDITIONAL_DEPS = "Additional_Dependencies";

  @Named(RESOLVER_INTERNAL_MAP)
  @Binds
  @IntoMap
  @ClassKey(OopMockConfiguration.class)
  Object bindsOopMockConfiguration(OopMockConfiguration configuration);

  @Named(RESOLVER_INTERNAL_MAP)
  @Binds
  @IntoMap
  @ClassKey(JsonConverter.class)
  Object bindsJsonConverter(JsonConverter converter);

  @Named(RESOLVER_INTERNAL_MAP)
  @Binds
  @IntoMap
  @ClassKey(ResourceLookupManager.class)
  Object bindsResourceLookupManager(ResourceLookupManager manager);

  @Named(RESOLVER_INTERNAL_MAP)
  @Binds
  @IntoMap
  @ClassKey(Translator.class)
  Object bindsTranslator(Translator translator);

  @Named(RESOLVER_INTERNAL_MAP)
  @Binds
  @IntoMap
  @ClassKey(Hasher.class)
  Object bindsHasher(Hasher hasher);

  @BindsOptionalOf
  @Named(DEFAULT_RESOLVER)
  String defaultResolver();

  @Module
  class ResolverConfigModule {

    private final Map<Class<?>, Object> additionalDependencies;

    public ResolverConfigModule() {
      this(ImmutableMap.of());
    }

    public ResolverConfigModule(final Map<Class<?>, Object> additionalDependencies) {
      this.additionalDependencies = additionalDependencies;
    }

    @Provides
    @Singleton
    @Named(RESOLVER_MAP)
    public Map<Class<?>, Object> fullDependencies(@Named(RESOLVER_ADDITIONAL_DEPS) final Map<Class<?>, Object> addition,
                                                  @Named(RESOLVER_INTERNAL_MAP) final Map<Class<?>, Object> internal,
                                                  final OopMockConfiguration configuration) {
      final ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();
      builder.putAll(internal);
      builder.putAll(addition);
      configuration.resolverConfiguration().ifPresent(rc -> builder.put(ResolverConfiguration.class, rc));
      return builder.build();
    }

    @Provides
    @Singleton
    @Named(RESOLVER_ADDITIONAL_DEPS)
    public Map<Class<?>, Object> additionalDependencies() {
      return additionalDependencies;
    }

    @Provides
    @Singleton
    @Named(RESOLVER_CLASSNAME)
    public String resolverClassName(@Named(DEFAULT_RESOLVER) final Optional<String> defaultResolver,
                                    final OopMockConfiguration configuration) {
      return configuration.resolverConfiguration()
          .map(ResolverConfiguration::resolverClass)
          .orElseGet(() -> defaultResolver
              .orElseThrow(() -> new IllegalArgumentException("No resolver found in configuration")));
    }
  }

}
