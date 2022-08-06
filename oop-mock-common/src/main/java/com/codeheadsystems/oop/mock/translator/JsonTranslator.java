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

package com.codeheadsystems.oop.mock.translator;

import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.model.ImmutableMockedData;
import com.codeheadsystems.oop.mock.model.MockedData;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JsonTranslator implements Translator {

  private final JsonConverter converter;

  @Inject
  public JsonTranslator(final JsonConverter converter) {
    this.converter = converter;
  }

  @Override
  public <R> R unmarshal(final Class<R> clazz,
                         final MockedData marshalledData) {
    return converter.convert(marshalledData.marshalledData(), clazz);
  }

  @Override
  public <R> MockedData marshal(final R object) {
    return ImmutableMockedData.builder()
        .marshalledData(converter.toJson(object))
        .build();
  }
}
