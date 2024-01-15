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

package com.codeheadsystems.keystore.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * Key as seen by the client.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableKey.class)
@JsonDeserialize(builder = ImmutableKey.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Key {

  /**
   * Owner string.
   *
   * @return the string
   */
  @JsonProperty("owner")
  String owner();

  /**
   * Id string.
   *
   * @return the string
   */
  @JsonProperty("id")
  String id();

  /**
   * Version long.
   *
   * @return the long
   */
  @JsonProperty("version")
  Long version();

  /**
   * Status string.
   *
   * @return the string
   */
  @JsonProperty("status")
  String status();

  /**
   * Used for de/encryption. Not protected. May be ignored
   *
   * @return the optional
   */
  @JsonProperty("aux")
  byte[] aux();

  /**
   * Key byte [ ].
   *
   * @return the byte [ ]
   */
  @JsonProperty("key")
  byte[] key();

}
