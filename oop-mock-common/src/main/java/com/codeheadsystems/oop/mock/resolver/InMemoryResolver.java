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
import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.manager.ResourceLookupManager;
import com.codeheadsystems.oop.mock.model.InMemoryMockedDataStore;
import com.codeheadsystems.oop.mock.model.MockedData;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InMemoryResolver implements MockDataResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryResolver.class);

  protected final Hasher hasher;
  protected final Map<String, Map<String, MockedData>> datastore;

  @Inject
  public InMemoryResolver(final OopMockConfiguration configuration,
                          final JsonConverter converter,
                          final ResourceLookupManager manager,
                          final Hasher hasher) {
    LOGGER.info("InMemoryResolver({})", configuration);
    this.hasher = hasher;
    final String filename = configuration.mockDataFileName()
        .orElseThrow(() -> new IllegalArgumentException("No filename found for inMemoryResolver"));
    final InputStream inputStream = manager.inputStream(filename)
        .orElseThrow(() -> new IllegalArgumentException("No such file for data store:" + filename));
    this.datastore = converter.convert(inputStream, InMemoryMockedDataStore.class).datastore();
  }

  @Override
  public Optional<MockedData> resolve(final String namespace,
                                      final String lookup,
                                      final String discriminator) {
    LOGGER.debug("resolve({},{},{})", namespace, lookup, discriminator);
    final Map<String, MockedData> discriminatorMap = datastore.get(namespace);
    if (discriminatorMap == null) {
      LOGGER.debug("-> no namespace");
      return Optional.empty();
    } else {
      final String aggregator = hasher.hash(lookup, discriminator);
      final MockedData mockedData = discriminatorMap.get(aggregator);
      LOGGER.debug("-> discriminator found: {}", mockedData != null);
      return Optional.ofNullable(mockedData);
    }
  }
}
