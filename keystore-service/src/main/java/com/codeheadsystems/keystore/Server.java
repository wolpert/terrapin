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

package com.codeheadsystems.keystore;

import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.keystore.module.DropWizardComponent;
import com.codeheadsystems.keystore.module.KeyStoreModule;
import com.codeheadsystems.keystore.server.dao.ddb.configuration.ImmutableTableConfiguration;
import com.codeheadsystems.keystore.server.dao.ddb.dagger.DdbModule;
import com.codeheadsystems.keystore.server.dao.ddb.manager.AwsManager;
import com.codeheadsystems.metrics.dagger.MetricsModule;
import com.codeheadsystems.metrics.helper.DropwizardMetricsHelper;
import dagger.Component;
import dagger.Module;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.net.URISyntaxException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * We use dagger to create the resources (and etc.) needed for the server before we initialize the server.
 * That way we can see initialization issues before dropwizard issues.
 */
@Singleton
public class Server extends Application<KeyStoreConfiguration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

  @Inject
  public Server() {
    LOGGER.info("Server()");
  }

  /**
   * Run the world.
   *
   * @param args from the command line.
   * @throws Exception if we could not start the server.
   */
  public static void main(String[] args) throws Exception {
    LOGGER.info("main({})", (Object) args);
    final Server server = new Server();
    server.run(args);
  }

  /**
   * Temporary ... need a better dev env to test the db.
   * For this to work, you need to:
   * <ol>
   *     <li>wget https://s3.us-west-2.amazonaws.com/dynamodb-local/dynamodb_local_latest.tar.gz</li>
   *     <li>tar xf dynamodb_local_latest.tar.gz</li>
   *     <li>java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb</li>
   * </ol>
   * To be replaced soon... so it works from gradle automatically
   *
   * @return
   */
  DynamoDbClient localClient() {
    final AwsCredentials credentials = AwsBasicCredentials.create("one", "two");
    final AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
    try {
      final DynamoDbClient client = DynamoDbClient.builder()
          .credentialsProvider(credentialsProvider)
          .region(Region.US_EAST_1)
          .endpointOverride(new URI("http://localhost:8000"))
          .build();
      try {
        new AwsManager(client, ImmutableTableConfiguration.builder().build()).createTable();
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
      return client;
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Should not have happened given the hardcoded url", e);
    }
  }

  @Override
  public void run(final KeyStoreConfiguration configuration,
                  final Environment environment) throws Exception {
    LOGGER.info("run({},{})", configuration, environment);
    final MeterRegistry meterRegistry = new DropwizardMetricsHelper().instrument(environment.metrics());
    final DropWizardComponent component = DaggerServer_KeystoreDynamoDbComponent.builder()
        .metricsModule(new MetricsModule(meterRegistry))
        .ddbModule(new DdbModule(localClient()))
        .build();
    for (Object resource : component.resources()) {
      LOGGER.info("Registering resource: " + resource.getClass().getSimpleName());
      environment.jersey().register(resource);
    }
    for (HealthCheck healthCheck : component.healthChecks()) {
      LOGGER.info("Registering healthCheck: " + healthCheck.getClass().getSimpleName());
      environment.healthChecks().register(healthCheck.getClass().getSimpleName(), healthCheck);
    }
  }

  /**
   * The dagger component to build.
   */
  @Singleton
  @Component(modules = {
      KeyStoreModule.class,
      DynamoDbModule.class,})
  public interface KeystoreDynamoDbComponent extends DropWizardComponent {

  }

  /**
   * Add local specific stuff here to dynamo db.
   */
  @Module(includes = DdbModule.class)
  public interface DynamoDbModule {

  }

}