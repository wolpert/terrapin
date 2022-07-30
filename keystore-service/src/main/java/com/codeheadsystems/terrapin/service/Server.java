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

package com.codeheadsystems.terrapin.service;

import com.codeheadsystems.terrapin.service.module.RNGModule;
import com.codeheadsystems.terrapin.service.module.ResourceModule;
import com.codeheadsystems.terrapin.service.resource.KeyStoreResource;
import dagger.Component;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Server extends Application<KeyStoreConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final Set<KeyStoreResource> resources;

    @Inject
    public Server(final ServerComponent component) {
        LOGGER.info("Server({})", component);
        this.resources = component.resources();
    }

    /**
     * Run the world.
     *
     * @param args from the command line.
     * @throws Exception if we could not start the server.
     */
    public static void main(String[] args) throws Exception {
        // TODO: The server configuration depends on the arguments.
        // So we need to get the configuration and use it to get the resources, metrics, healthchecks, etc.
        // Not build the server yet.
        LOGGER.info("main({})", (Object) args);
        final ServerComponent component = DaggerServer_ServerComponent.builder()
                .build();
        final Server server = new Server(component);
        server.run(args);
    }

    @Override
    public void run(final KeyStoreConfiguration configuration,
                    final Environment environment) throws Exception {

        LOGGER.info("run({},{})", configuration, environment);
        // TODO: Add health checks.
        // TODO: Add metrics
        //environment.healthChecks().register("template", healthCheck);
        for (Object resource : resources) {
            LOGGER.info("Registering resource: " + resource.getClass().getSimpleName());
            environment.jersey().register(resource);
        }
    }

    @Singleton
    @Component(modules = {RNGModule.class, ResourceModule.class})
    public interface ServerComponent {
        Set<KeyStoreResource> resources();

    }

}