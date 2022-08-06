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

package com.codeheadsystems.oop.dao.ddb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.codeheadsystems.oop.client.dao.MockDataDAO;
import com.codeheadsystems.oop.dao.ddb.converter.DDBEntryConverter;
import com.codeheadsystems.oop.dao.ddb.model.DDBEntry;
import com.codeheadsystems.oop.mock.model.MockedData;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MockDataDDBDAO implements MockDataDAO {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockDataDDBDAO.class);
  private final DynamoDBMapper mapper;
  private DDBEntryConverter converter;

  @Inject
  public MockDataDDBDAO(final DynamoDBMapper mapper,
                        final DDBEntryConverter converter) {
    this.mapper = mapper;
    this.converter = converter;
    LOGGER.info("MockDataDDBDAO({},{})", mapper, converter);
  }

  @Override
  public void store(final String namespace,
                    final String lookup,
                    final String discriminator,
                    final MockedData data) {
    mapper.save(converter.convert(namespace, lookup, discriminator, data));
  }

  @Override
  public void delete(final String namespace,
                     final String lookup,
                     final String discriminator) {
    mapper.delete(converter.convert(namespace, lookup, discriminator));
  }

  @Override
  public Optional<MockedData> resolve(final String namespace,
                                      final String lookup,
                                      final String discriminator) {
    final DDBEntry entry = mapper.load(converter.convert(namespace, lookup, discriminator));
    if (entry == null) {
      return Optional.empty();
    } else {
      return converter.toMockedData(entry);
    }
  }
}
