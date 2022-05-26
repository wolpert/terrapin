// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dao.ddb;

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
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamoDBExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, ParameterResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBExtension.class);

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(DynamoDBExtension.class);
    private static final Class<?> SERVER = DynamoDBProxyServer.class;
    private static final Class<?> CLIENT = AmazonDynamoDB.class;
    private static final Class<?> MAPPER = DynamoDBMapper.class;

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

    private void withStore(final ExtensionContext context,
                           final Consumer<ExtensionContext.Store> consumer) {
        consumer.accept(context.getStore(NAMESPACE));
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext,
                                     final ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(LocalDynamoDB.class);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext,
                                   final ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(CLIENT);
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

    @Override
    public void beforeEach(final ExtensionContext context) {
        context.getRequiredTestInstances().getAllInstances().forEach(o -> {
            Arrays.stream(o.getClass().getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(LocalDynamoDB.class))
                    .forEach(field -> {
                        setValueForField(context, o, field);
                    });
        });
    }

    private void setValueForField(final ExtensionContext context,
                                  final Object o,
                                  final Field field) {
        withStore(context, s -> {
            final Object value = s.get(field.getType()); // Check the store to see we have this type.
            if (value != null) { // Good, go set it.
                enableSettingTheField(field);
                try {
                    field.set(o, value);
                } catch (IllegalAccessException e) {
                    LOGGER.error("Unable to set the field value for {}", field.getName(), e);
                    LOGGER.error("Continuing, but expect nothing good will happen next.");
                }
            } else { // Too bad. Fail loudly so the dev can fix it.
                LOGGER.error("Type {} is unknown to the DynamoDB extension. You have the annotation on the wrong field", field.getType());
                throw new IllegalArgumentException("Unable to find DynamoDB extension value of type " + field.getType());
            }
        });
    }

    /**
     * This allows us to set the field directly. It will fail if the security manager in play disallows it.
     * We can talk about justifications all we want, but really we know Java is not Smalltalk. Meta programming
     * is limited here. So... we try to do the right thing.
     *
     * @param field to change accessibility for.
     */
    private void enableSettingTheField(final Field field) {
        try {
            field.setAccessible(true);
        } catch (RuntimeException re) {
            LOGGER.error("Unable to change accessibility for field due to private var or security manager: {}", field.getName());
            LOGGER.error("The setting will likely fail. Consider changing that type to protected.", re);
        }
    }
}
