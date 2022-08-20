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
import com.codeheadsystems.keystore.config.KeyStoreConfiguration;
import com.codeheadsystems.keystore.server.dao.casssandra.dagger.CassandraModule;
import com.codeheadsystems.keystore.server.dao.casssandra.dagger.CqlSessionModule;
import com.codeheadsystems.metrics.dagger.MetricsModule;
import dagger.Component;
import dagger.Module;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import javax.inject.Singleton;

public class CassandraServer implements DropWizardFactory {

  private static void getAddress(final KeyStoreConfiguration configuration) {
    try {
      final URI uri = new URI(configuration.getDataStore().connectionUrl());
      final InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DropWizardComponent build(final KeyStoreConfiguration configuration,
                                   final MeterRegistry meterRegistry) {
    getAddress(configuration);
    return DaggerCassandraServer_CassandraComponent.builder()
        .cqlSessionModule(new CqlSessionModule())
        .metricsModule(new MetricsModule(meterRegistry))
        .build();
  }

  /**
   * The dagger component to build.
   */
  @Singleton
  @Component(modules = {
      CassandraComponent.AuxModule.class,
      CassandraModule.class,
      KeyStoreModule.class})
  public interface CassandraComponent extends DropWizardComponent {

    /**
     * Add local specific stuff here to dynamo db.
     */
    @Module
    class AuxModule {
    }

  }

}
