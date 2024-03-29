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

package com.codeheadsystems.keystore.server.dao.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * A value wrapper for a token. To ensure it doesn't get lost in the shuffle.
 * Tokens may contain data results and should be treated as such. So creating this
 * class to manage tokens correctly.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableToken.class)
@JsonDeserialize(builder = ImmutableToken.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Token {

  /**
   * Value string.
   *
   * @return the string
   */
  @JsonProperty("value")
  String value();

}
