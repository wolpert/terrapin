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

package com.codeheadsystems.keystore.server.dao.ddb.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Table configuration for dynamodb.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTableConfiguration.class)
@JsonDeserialize(builder = ImmutableTableConfiguration.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface TableConfiguration {

  @Value.Default
  @JsonProperty("tableName")
  default String tableName() {
    return "keyservice";
  }

  @Value.Default
  @JsonProperty("hashKey")
  default String hashKey() {
    return "hashKey";
  }

  @Value.Default
  @JsonProperty("rangeKey")
  default String rangeKey() {
    return "rangeKey";
  }

  @Value.Default
  @JsonProperty("ttlKey")
  default String ttlKey() {
    return "ttl";
  }

  /**
   * The index for all active key versions.
   */
  @Value.Default
  @JsonProperty("activeIndex")
  default String activeIndex() {
    return "activeIndex";
  }

  /**
   * The index for every key record that is owned.
   */
  @Value.Default
  @JsonProperty("ownerIndex")
  default String ownerIndex() {
    return "ownerIndex";
  }

  /**
   * The index for every owner.
   */
  @Value.Default
  @JsonProperty("ownerSearchIndex")
  default String ownerSearchIndex() {
    return "ownerSearchIndex";
  }

}
