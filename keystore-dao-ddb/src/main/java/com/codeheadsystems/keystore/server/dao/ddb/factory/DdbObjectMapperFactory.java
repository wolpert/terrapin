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

package com.codeheadsystems.keystore.server.dao.ddb.factory;

import com.codeheadsystems.keystore.common.factory.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javax.inject.Inject;
import javax.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Singleton
public class DdbObjectMapperFactory {

  private final ObjectMapperFactory factory;

  @Inject
  public DdbObjectMapperFactory(final ObjectMapperFactory factory) {
    this.factory = factory;
  }

  /**
   * This one handles the AttributeValue, in addition to the regular modules.
   *
   * @return an object mapper suitable for us.
   */
  public ObjectMapper generate() {
    final SimpleModule simpleModule = new SimpleModule();
    simpleModule.addAbstractTypeMapping(AttributeValue.Builder.class, AttributeValue.serializableBuilderClass());
    return factory.generate()
        .registerModule(simpleModule);
  }

}
