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

package com.codeheadsystems.test.datastore;

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
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamoDBExtension extends DataStoreExtension implements BeforeAllCallback, AfterAllCallback,  ParameterResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBExtension.class);

    private static final Class<?> SERVER = DynamoDBProxyServer.class;
    private static final Class<?> CLIENT = AmazonDynamoDB.class;
    private static final Class<?> MAPPER = DynamoDBMapper.class;

    @Override protected Class<?> namespaceClass() {
        return DynamoDBExtension.class;
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        LOGGER.info("Tearing down in memory DynamoDB local instance");
        withStore(context, s -> {
            try {
                s.remove(SERVER, DynamoDBProxyServer.class).stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            s.remove(CLIENT);
            s.remove(MAPPER);
        });
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        LOGGER.info("Setting in memory DynamoDB local instance");
        String port = "8000";
        DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-inMemory", "-port", port});
        server.start();
        withStore(context, s -> {
            AmazonDynamoDB client = getAmazonDynamoDB();
            s.put(SERVER, server);
            s.put(CLIENT, client);
            s.put(MAPPER, new DynamoDBMapper(client));
        });
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext,
                                     final ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(DataStore.class);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext,
                                   final ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(namespace).get(CLIENT);
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
