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

package com.codeheadsystems.terrapin.keystore;

import static com.codeheadsystems.metrics.dagger.MetricsModule.METER_REGISTRY;

import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.terrapin.keystore.module.KeyStoreModule;
import com.codeheadsystems.terrapin.keystore.resource.JettyResource;
import dagger.Component;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Override
    public void run(final KeyStoreConfiguration configuration,
                    final Environment environment) throws Exception {

        LOGGER.info("run({},{})", configuration, environment);
        // TODO: setup metrics
        final KeystoreComponent component = DaggerServer_KeystoreComponent.builder()
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

    @Singleton
    @Component(modules = {KeyStoreModule.class})
    public interface KeystoreComponent {

        Set<JettyResource> resources();

        Set<HealthCheck> healthChecks();

        @Named(METER_REGISTRY)
        MeterRegistry metricRegistry();
    }

}