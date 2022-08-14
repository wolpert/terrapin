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

package com.codeheadsystems.keystore.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.keystore.server.dao.ddb.configuration.ImmutableTableConfiguration;
import com.codeheadsystems.keystore.server.dao.ddb.configuration.TableConfiguration;
import com.codeheadsystems.keystore.server.dao.ddb.manager.AWSManager;
import com.codeheadsystems.test.datastore.DataStore;
import com.codeheadsystems.test.datastore.DynamoDbExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;

@ExtendWith(DynamoDbExtension.class)
class AWSManagerTest {

  @DataStore private DynamoDbClient client;
  private TableConfiguration tableConfiguration = ImmutableTableConfiguration.builder().build();
  private AWSManager manager;

  @BeforeEach
  public void setup() {
    manager = new AWSManager(client, tableConfiguration);
  }

  @AfterEach
  public void cleanup() {
    client.deleteTable(DeleteTableRequest.builder()
        .tableName(tableConfiguration.tableName())
        .build());
  }

  @Test
  public void createTable() {
    manager.createTable();

    assertThat(client.listTables().tableNames())
        .hasSize(1)
        .containsExactly(tableConfiguration.tableName());
  }

}