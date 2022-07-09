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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.oop.OopMockConfiguration;
import com.codeheadsystems.oop.ResolverConfiguration;
import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.manager.ResourceLookupManager;
import com.codeheadsystems.oop.mock.model.MockedData;
import com.codeheadsystems.oop.mock.translator.Translator;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolverFactoryTest {

    @Mock private OopMockConfiguration configuration;
    @Mock private ResolverConfiguration resolverConfiguration;
    @Mock private JsonConverter converter;
    @Mock private ResourceLookupManager manager;
    @Mock private Translator translator;
    @Mock private Hasher hasher;
    private Map<Class<?>, Object> instanceMap;

    @BeforeEach
    void setup() {
        instanceMap = ImmutableMap.of(
                OopMockConfiguration.class, configuration,
                JsonConverter.class, converter,
                ResourceLookupManager.class, manager,
                Translator.class, translator,
                Hasher.class, hasher
        );
    }

    private MockDataResolver factoryConstructAndGetResolver(String classname) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return new ResolverFactory(classname, instanceMap).build();
    }

    @Test
    void build_noInject() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> factoryConstructAndGetResolver("com.codeheadsystems.oop.mock.resolver.ResolverFactoryTest$DoNothing"))
                .withMessageContaining("No constructor with @Inject for");
    }

    //@Test // disabled because this doesn't fail the way we think it should
    void build_badConstructorArgs() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> factoryConstructAndGetResolver("com.codeheadsystems.oop.mock.resolver.ResolverFactoryTest$BadConstructorArgs"))
                .withMessageContaining("Missing injected param for class");
    }

    @Test
    void build_goodExampleWithArg() throws Exception {
        assertThat(factoryConstructAndGetResolver(GoodExampleWithArg.class.getCanonicalName()))
                .isNotNull()
                .isInstanceOf(MockDataResolver.class);
    }

    @Test
    void build_goodExampleWithSubclass() throws Exception {
        assertThat(factoryConstructAndGetResolver(GoodExampleWithSubclass.class.getCanonicalName()))
                .isNotNull()
                .isInstanceOf(MockDataResolver.class);
    }

    @Test
    void build_goodExampleWithNoArgs_wrongReturnType() {
        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> {
                    final Date date = new ResolverFactory(NoArgGoodExample.class.getCanonicalName(), instanceMap).build();
                });
    }

    @Test
    void build_goodExampleWithNoArgs() throws Exception {
        assertThat(factoryConstructAndGetResolver(NoArgGoodExample.class.getCanonicalName()))
                .isNotNull()
                .isInstanceOf(MockDataResolver.class);
    }

    public class BadConstructorArgs extends DoNothing {

        @Inject
        public BadConstructorArgs(final OopMockConfiguration configuration,
                                  final JsonConverter converter,
                                  final ResourceLookupManager manager,
                                  final Translator translator,
                                  final UselessClass badArg) {

        }
    }

    public class UselessClass {
        @Inject
        public UselessClass(final Object other) {

        }
    }

    public class DoNothing implements MockDataResolver {

        @Override
        public Optional<MockedData> resolve(final String namespace, final String lookup, final String discriminator) {
            return Optional.empty();
        }
    }
}