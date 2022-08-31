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

package com.codeheadsystems.keystore.dagger;

import com.codeheadsystems.keystore.DropWizardFactory;
import com.codeheadsystems.keystore.config.DataStore;
import com.codeheadsystems.keystore.config.KeyStoreConfiguration;
import com.codeheadsystems.keystore.server.dao.ddb.configuration.ImmutableTableConfiguration;
import com.codeheadsystems.keystore.server.dao.ddb.dagger.DdbModule;
import com.codeheadsystems.keystore.server.dao.ddb.manager.AwsManager;
import com.codeheadsystems.metrics.dagger.MetricsModule;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.net.URISyntaxException;
import javax.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Builds out a drop wizard component where the backend is dynamodb.
 */
public class DynamoDbServer implements DropWizardFactory {

  @Override
  public DropWizardComponent build(final KeyStoreConfiguration configuration,
                                   final MeterRegistry meterRegistry) {
    return DaggerDynamoDbServer_KeystoreDynamoDbComponent.builder()
        .keyStoreModule(new KeyStoreModule(configuration))
        .metricsModule(new MetricsModule(meterRegistry))
        .build();
  }

  /**
   * The dagger component to build.
   */
  @Singleton
  @Component(modules = {
      KeystoreDynamoDbComponent.AuxModule.class,
      DdbModule.class,
      KeyStoreModule.class})
  public interface KeystoreDynamoDbComponent extends DropWizardComponent {

    /**
     * Add local specific stuff here to dynamo db.
     */
    @Module
    class AuxModule {

      /**
       * Returns the client based on the configuration. Really we need to figure out what to do in production on AWS.
       *
       * @param configuration for the keystore.
       * @return a db client.
       */
      @Provides
      @Singleton
      public DynamoDbClient localClient(final KeyStoreConfiguration configuration) {
        final DataStore dataStore = configuration.getDataStore();
        final AwsCredentials credentials = AwsBasicCredentials.create(dataStore.username(), dataStore.password());
        final AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        try {
          final DynamoDbClient client = DynamoDbClient.builder()
              .credentialsProvider(credentialsProvider)
              .region(Region.US_EAST_1)
              .endpointOverride(new URI(dataStore.connectionUrl()))
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
    }

  }

}
