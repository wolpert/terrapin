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

package com.codeheadsystems.keystore.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import org.immutables.value.Value;

/**
 * The generic datastore configuration. Remember, we do not use the dropwizard SQL driver for non-SQL DAOs.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDataStore.class)
@JsonDeserialize(builder = ImmutableDataStore.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface DataStore {

  @JsonProperty("connectionUrl")
  String connectionUrl();

  @JsonProperty("username")
  String username();

  @JsonProperty("password")
  String password();

  @JsonProperty("aux")
  Map<String, String> aux();

}
