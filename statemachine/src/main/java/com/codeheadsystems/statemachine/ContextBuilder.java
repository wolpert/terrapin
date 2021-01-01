/*
 *    Copyright (c) 2021 Ned Wolpert <ned.wolpert@gmail.com>
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

package com.codeheadsystems.statemachine;

import com.codahale.metrics.MetricRegistry;
import com.codeheadsystems.statemachine.factory.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for the Context.
 * <p>
 * Uses Dagger as the IOC framework, so no runtime cost for consumers of the StateMachine context.
 */
public class ContextBuilder {

    private static final Logger log = LoggerFactory.getLogger(ContextBuilder.class);

    private MetricRegistry metricRegistry;

    /**
     * Add in your own metric registry if you want.
     *
     * @param metricRegistry if you have a common one.
     * @return builder.
     */
    public ContextBuilder metricRegistry(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        return this;
    }

    public Context build() {
        log.info("[ContextBuilder] build()");
        final ContextComponent component = DaggerContextBuilder_ContextComponent.builder()
            .stateMachineModules(new StateMachineModules(metricRegistry))
            .build();
        return component.context();
    }

    @Singleton
    @Component(modules = {StateMachineModules.class})
    public interface ContextComponent {

        Context context();

    }

    @Module
    public static class StateMachineModules {

        private final MetricRegistry metricRegistry;

        public StateMachineModules(final MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
        }

        @Provides
        @Singleton
        public MetricRegistry metricRegistry() {
            if (metricRegistry == null) {
                return new MetricRegistry();
            } else {
                return metricRegistry;
            }
        }

        @Provides
        @Singleton
        public ObjectMapper objectMapper(final ObjectMapperFactory factory) {
            return factory.objectMapper();
        }

    }

}
