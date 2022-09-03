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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.keystore.config.KeyStoreConfiguration;
import com.codeheadsystems.keystore.dagger.DropWizardComponent;
import com.codeheadsystems.keystore.resource.JettyResource;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Set;

public class ServerTestDropwizardFactory implements DropWizardFactory{

  private final DropWizardComponent dropWizardComponent;
  private final JettyResource jettyResource;
  private final HealthCheck healthCheck;

  public ServerTestDropwizardFactory() {
    dropWizardComponent = mock(DropWizardComponent.class);
    jettyResource = mock(JettyResource.class);
    healthCheck = mock(HealthCheck.class);
  }

  @Override
  public DropWizardComponent build(final KeyStoreConfiguration configuration, final MeterRegistry meterRegistry) {
    when(dropWizardComponent.healthChecks())
        .thenReturn(Set.of(healthCheck));
    when(dropWizardComponent.resources())
        .thenReturn(Set.of(jettyResource));
    return dropWizardComponent;
  }

}
