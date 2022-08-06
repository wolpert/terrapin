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

import static com.codeheadsystems.oop.mock.manager.ResourceLookupManager.LOOKUP_CLASS;

import com.codeheadsystems.oop.OopMockConfiguration;
import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.factory.ObjectMapperFactory;
import com.codeheadsystems.oop.mock.translator.JsonTranslator;
import com.codeheadsystems.oop.mock.translator.Translator;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;

@Module(includes = {StandardModule.BindingsModule.class, OopConfigurationModule.class, ResolverModule.class})
public class StandardModule {

  public static final String OOP_SYSTEM = "OOP_SYSTEM";
  public static final String NAMESPACE = "DEFAULT";

  /**
   * If the namespace is configured in the Dagger environment, we will use that. Else we will use what
   * is in the configuration file. Note that the configuration file pulls the default namespace from
   * here.
   */
  @Provides
  @Singleton
  public Hasher hasher(@Named(OOP_SYSTEM) final Optional<String> system,
                       final OopMockConfiguration configuration) {
    return new Hasher(system.orElse(configuration.namespace()));
  }

  @Provides
  @Singleton
  public ObjectMapper objectMapper(final ObjectMapperFactory factory) {
    return factory.objectMapper();
  }

  @Provides
  @Singleton
  public Translator translator(final JsonTranslator translator) {
    return translator;
  }

  @Module
  interface BindingsModule {

    /**
     * Provides the system namespace to set. If not set, it will be default: DEFAULT
     *
     * @return system name.
     */
    @BindsOptionalOf
    @Named(OOP_SYSTEM)
    String systemName();

    /**
     * Set this if you want to use your own classloader to base the lookup on. Needed if the
     * ResourceLookupManager cannot find the resource you need.
     *
     * @return an instance.
     * @see com.codeheadsystems.oop.mock.manager.ResourceLookupManager
     */
    @BindsOptionalOf
    @Named(LOOKUP_CLASS)
    ClassLoader lookupClassLoader();
  }

}
