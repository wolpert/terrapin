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

package com.codeheadsystems.oop.mock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableInMemoryMockedDataStore.class)
@JsonDeserialize(builder = ImmutableInMemoryMockedDataStore.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface InMemoryMockedDataStore {

  /**
   * Provides an inmemory datastore of all mocked data. When in use, this should  be loaded up on
   * boot and used. Note that this is mostly used for testing or writing mocked data. Unless your
   * needs are small, you likely want a real datastore.
   *
   * @return Namespace->discriminator->mocked data map. (discriminator = lookup.id as processed by hasher)
   */
  Map<String, Map<String, MockedData>> datastore();

}
