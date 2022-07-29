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

import com.codeheadsystems.terrapin.service.model.TerrapinConfiguration;
import com.codeheadsystems.terrapin.service.module.DaggerServerComponent;
import com.codeheadsystems.terrapin.service.resource.KeyStoreResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TerrapinServer extends Application<TerrapinConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TerrapinServer.class);
    private final Set<KeyStoreResource> resources;

    @Inject
    public TerrapinServer(final Set<KeyStoreResource> resources) {
        LOGGER.info("TerrapinServer({})", resources);
        this.resources = resources;
    }

    /**
     * Run the world.
     *
     * @param args from the command line.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        LOGGER.info("main({})", (Object) args);
        final TerrapinServer server = DaggerServerComponent.builder()
                .build()
                .terrapinServer();
        server.run(args);
    }

    @Override
    public void run(final TerrapinConfiguration configuration,
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
}