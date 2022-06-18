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

package com.codeheadsystems.oop.dao.ddb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.codeheadsystems.oop.ImmutableResolverConfiguration;
import com.codeheadsystems.oop.ResolverConfiguration;
import com.codeheadsystems.oop.dao.ddb.model.DDBEntry;
import com.codeheadsystems.oop.test.FullDAOTest;
import com.codeheadsystems.test.datastore.DataStore;
import com.codeheadsystems.test.datastore.DynamoDBExtension;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DynamoDBExtension.class)
public class FullDDBDAOTest extends FullDAOTest {

    @DataStore private DynamoDBMapper mapper;
    @DataStore private AmazonDynamoDB amazonDynamoDB;

    @BeforeEach
    void setup() {
        amazonDynamoDB.createTable(mapper.generateCreateTableRequest(DDBEntry.class)
            .withBillingMode(BillingMode.PAY_PER_REQUEST));
    }

    @AfterEach
    void tearDown() {
        // force the table empty
        amazonDynamoDB.deleteTable(mapper.generateDeleteTableRequest(DDBEntry.class));
    }

    @Override
    protected ResolverConfiguration resolverConfiguration() {
        return ImmutableResolverConfiguration.builder()
            .resolverClass(MockDataDDBDAO.class.getCanonicalName())
            .build();
    }

    @Override
    protected Map<Class<?>, Object> resolverDeps() {
        return ImmutableMap.of(DynamoDBMapper.class, mapper);
    }
}
