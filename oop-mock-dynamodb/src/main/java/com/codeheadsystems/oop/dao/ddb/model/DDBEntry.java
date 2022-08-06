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

package com.codeheadsystems.oop.dao.ddb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "oop_mock_data")
public class DDBEntry {

  private String hash;
  private String range;
  private String mockData;
  private Long ttl;

  public DDBEntry() {
  }

  public DDBEntry(final String hash, final String range) {
    this.hash = hash;
    this.range = range;
  }

  public DDBEntry(final String hash, final String range, final String mockData) {
    this.hash = hash;
    this.range = range;
    this.mockData = mockData;
  }

  @DynamoDBHashKey(attributeName = "hash")
  public String getHash() {
    return hash;
  }

  public void setHash(final String hash) {
    this.hash = hash;
  }

  @DynamoDBRangeKey(attributeName = "range")
  public String getRange() {
    return range;
  }

  public void setRange(final String range) {
    this.range = range;
  }

  @DynamoDBAttribute(attributeName = "mock_data")
  public String getMockData() {
    return mockData;
  }

  public void setMockData(final String mockData) {
    this.mockData = mockData;
  }

  @DynamoDBAttribute(attributeName = "ttl")
  public Long getTtl() {
    return ttl;
  }

  public void setTtl(final Long ttl) {
    this.ttl = ttl;
  }

}
