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

package com.codeheadsystems.oop.dao.ddb.converter;

import com.codeheadsystems.oop.dao.ddb.model.DDBEntry;
import com.codeheadsystems.oop.mock.Hasher;
import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.model.MockedData;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DDBEntryConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DDBEntryConverter.class);

  private final Hasher hasher;
  private final JsonConverter jsonConverter;

  @Inject
  public DDBEntryConverter(final Hasher hasher,
                           final JsonConverter jsonConverter) {
    this.hasher = hasher;
    this.jsonConverter = jsonConverter;
    LOGGER.info("DDBEntryConverter({},{})", hasher, jsonConverter);
  }

  public DDBEntry convert(final String namespace,
                          final String lookup,
                          final String discriminator,
                          final MockedData data) {
    final DDBEntry entry = convert(namespace, lookup, discriminator);
    final String json = jsonConverter.toJson(data);
    entry.setMockData(json);
    return entry;
  }

  public DDBEntry convert(final String namespace,
                          final String lookup,
                          final String discriminator) {
    final String secondary = hasher.hash(lookup, discriminator);
    return new DDBEntry(namespace, secondary);
  }

  public Optional<MockedData> toMockedData(final DDBEntry entry) {
    if (entry.getMockData() == null) {
      return Optional.empty();
    } else {
      return Optional.of(jsonConverter.convert(entry.getMockData(), MockedData.class));
    }
  }

}
