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

import com.codeheadsystems.oop.ImmutableOopMockConfiguration;
import com.codeheadsystems.oop.OopMockConfiguration;
import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.manager.ResourceLookupManager;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Module
public class OopConfigurationModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(OopConfigurationModule.class);
    public static final String CONFIGURATION_FILENAME = "oopMockConfiguration.json";

    private final String configurationFileName;
    private final OopMockConfiguration configuration;

    public OopConfigurationModule() {
        this(CONFIGURATION_FILENAME);
    }

    public OopConfigurationModule(final String configurationName) {
        this.configurationFileName = configurationName;
        this.configuration = null;
    }

    public OopConfigurationModule(final OopMockConfiguration configuration) {
        this.configuration = configuration;
        this.configurationFileName = null;
    }

    @Provides
    @Singleton
    public OopMockConfiguration configuration(final ResourceLookupManager manager,
                                              final JsonConverter converter) {
        if (configuration != null) {
            return configuration;
        }
        return manager.inputStream(configurationFileName)
                .map(is -> converter.convert(is, OopMockConfiguration.class))
                .orElseGet(this::defaultOopMockConfiguration);
    }

    private OopMockConfiguration defaultOopMockConfiguration() {
        LOGGER.warn("No configuration found, using default disabled configuration");
        return ImmutableOopMockConfiguration.builder().build();
    }
}
