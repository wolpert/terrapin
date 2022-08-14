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

import com.codeheadsystems.keystore.server.dao.Changable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * The key represents the data need to (de|en)crypt data. The ID is a UUID that is unique to the groupId.
 * Keys are versioned. So one 'key' has many 'versions'. What is represented here is a single version. We
 * do not represent the list of keys that are available.
 * The identifier for a key is 'groupId:keyId:version'
 */
@Value.Immutable
@JsonSerialize(as = ImmutableKey.class)
@JsonDeserialize(builder = ImmutableKey.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Key {

  /**
   * The pieces that make up the identity of the key.
   *
   * @return key identifier.
   */
  @JsonProperty("keyVersionIdentifier")
  KeyVersionIdentifier keyVersionIdentifier();

  /**
   * The value of the key.
   *
   * @return byte array.
   */
  @JsonProperty("value")
  byte[] value();

  /**
   * Boolean if the key is active.
   *
   * @return boolean.
   */
  @JsonProperty("active")
  @Changable
  Boolean active();

  /**
   * Keys can have types. (AES-GCM-SIV) The type cannot change between versions.
   *
   * @return String
   */
  @JsonProperty("type")
  String type();

  /**
   * Date the key was created.
   *
   * @return create date.
   */
  @JsonProperty("createDate")
  Date createDate();

  /**
   * Update date of the key, if ever updated. Only set when the key activation status is changed.
   * Set by the dao.
   *
   * @return update date.
   */
  @JsonProperty("updateDate")
  Optional<Date> updateDate();
}
