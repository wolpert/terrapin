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

package com.codeheadsystems.statemachine.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Standard way to generate an object mapper with the requirements for this project.
 */
@Singleton
public class ObjectMapperFactory {

  @Inject
  public ObjectMapperFactory() {

  }

  public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new Jdk8Module());
  }

}
