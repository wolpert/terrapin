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

import static org.mockito.Mockito.when;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codeheadsystems.keystore.config.KeyStoreConfiguration;
import com.codeheadsystems.keystore.dagger.DropWizardComponent;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServerTest {

  @Mock private DropWizardComponent dropWizardComponent;
  @Mock private KeyStoreConfiguration keyStoreConfiguration;
  @Mock private Environment environment;
  @Mock private JerseyEnvironment jerseyEnvironment;
  @Mock private HealthCheckRegistry healthCheckRegistry;
  @Mock private MetricRegistry metricRegistry;

  private Server server;

  @BeforeEach
  public void setup() {
    server = new Server();
  }

  @Test
  public void testRun() throws Exception {
    when(environment.metrics()).thenReturn(metricRegistry);
    when(keyStoreConfiguration.getDropWizardFactory()).thenReturn(ServerTestDropwizardFactory.class.getName());
    when(environment.jersey()).thenReturn(jerseyEnvironment);
    when(environment.healthChecks()).thenReturn(healthCheckRegistry);

    server.run(keyStoreConfiguration, environment);
  }

}