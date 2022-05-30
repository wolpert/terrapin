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

package com.codeheadsystems.oop.test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.codeheadsystems.oop.dao.ddb.model.DDBEntry;

public class Database {

    private DynamoDBProxyServer server;
    final private AmazonDynamoDB dynamoDB;
    final private DynamoDBMapper mapper;

    public Database() {
        dynamoDB = getAmazonDynamoDB();
        mapper = new DynamoDBMapper(dynamoDB);
    }

    public DynamoDBMapper dynamoDBMapper() {
        return mapper;
    }

    public AmazonDynamoDB amazonDynamoDB() {
        return dynamoDB;
    }

    public void setup() {
        try {
            String port = "8000";
            server = ServerRunner.createServerFromCommandLineArgs(
                    new String[]{"-inMemory", "-port", port});
            server.start();
            dynamoDB.createTable(mapper.generateCreateTableRequest(DDBEntry.class)
                    .withBillingMode(BillingMode.PAY_PER_REQUEST));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to setup database", e);
        }

    }

    public void teardown() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to stop database", e);
        }
    }

    private AmazonDynamoDB getAmazonDynamoDB() {
        final AWSCredentials credentials = new BasicAWSCredentials("one", "two");
        final AWSCredentialsProvider provider = new AWSStaticCredentialsProvider(credentials);
        final AwsClientBuilder.EndpointConfiguration configuration = new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2");

        return AmazonDynamoDBClientBuilder.standard()
                .withCredentials(provider)
                .withEndpointConfiguration(configuration)
                .build();
    }
}
