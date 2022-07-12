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

package com.codeheadsystems.terrapin.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.terrapin.server.dao.accessor.DynamoDbClientAccessor;
import com.codeheadsystems.terrapin.server.dao.converter.KeyConverter;
import com.codeheadsystems.test.datastore.DataStore;
import com.codeheadsystems.test.datastore.DynamoDBExtension;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;

@ExtendWith(DynamoDBExtension.class)
public class KeyDAODynamoDBTest extends KeyDAOTest {

    @DataStore private DynamoDbClient client;
    private TableConfiguration tableConfiguration = ImmutableTableConfiguration.builder().build();

    @Override
    protected KeyDAO keyDAO() {
        final RetryRegistry registry = RetryRegistry.ofDefaults();
        final Retry retry = registry.retry("retry.KeyDAODynamoDBTest");
        final DynamoDbClientAccessor accessor = new DynamoDbClientAccessor(client, metrics, retry);
        return new KeyDAODynamoDB(accessor, tableConfiguration, new KeyConverter(tableConfiguration));
    }

    @BeforeEach
    public void setupDatabase() {
        new AWSManager(client, tableConfiguration).createTable();
    }

    @AfterEach
    public void cleanup() {
        client.deleteTable(DeleteTableRequest.builder()
                .tableName(tableConfiguration.tableName())
                .build());
    }


    @Test
    public void testClient() {
        assertThat(client)
                .isNotNull();
        assertThat(client.listTables().tableNames())
                .isNotEmpty();
    }

}