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

package com.codeheadsystems.statemachine;

import com.codahale.metrics.MetricRegistry;
import com.codeheadsystems.statemachine.factory.ObjectMapperFactory;
import com.codeheadsystems.statemachine.manager.LockManager;
import com.codeheadsystems.statemachine.manager.MetricManager;
import com.codeheadsystems.statemachine.manager.impls.CodahaleMetricManager;
import com.codeheadsystems.statemachine.manager.impls.NullLockManager;
import com.codeheadsystems.statemachine.manager.impls.NullMetricManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.util.Set;
import javax.inject.Named;
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
  private final ImmutableSet.Builder<Hook.PendingTransition> pendingTransitionsBuilder = ImmutableSet.builder();
  private final ImmutableSet.Builder<Hook.PostTransition> postTransitionsBuilder = ImmutableSet.builder();
  private MetricRegistry metricRegistry;
  private MetricManager metricManager;
  private LockManager lockManager;

  /**
   * Add all of the global pending transition hooks to the context.
   *
   * @param set of hooks.
   * @return builder.
   */
  public ContextBuilder pendingTransitions(final Set<Hook.PendingTransition> set) {
    pendingTransitionsBuilder.addAll(set);
    return this;
  }

  /**
   * Add all of the global post transition hooks to the context.
   *
   * @param set of hooks.
   * @return builder.
   */
  public ContextBuilder postTransitions(final Set<Hook.PostTransition> set) {
    postTransitionsBuilder.addAll(set);
    return this;
  }

  /**
   * Add in your own lock manager
   *
   * @param lockManager to use.
   * @return builder.
   */
  public ContextBuilder lockManager(final LockManager lockManager) {
    this.lockManager = lockManager;
    return this;
  }

  /**
   * Add in your own metric registry if you want. This will use the default metric manager.
   *
   * @param metricRegistry if you have a common one.
   * @return builder.
   */
  public ContextBuilder metricManager(final MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
    return this;
  }

  /**
   * Add in your own metric manager if you want.
   *
   * @param metricManager if you have  one.
   * @return builder.
   */
  public ContextBuilder metricManager(final MetricManager metricManager) {
    this.metricManager = metricManager;
    return this;
  }

  public Context build() {
    log.info("[ContextBuilder] build()");
    final MetricManager resolvedMetricManager = resolveMetricManager();
    final ContextComponent component = DaggerContextBuilder_ContextComponent.builder()
        .stateMachineModules(new StateMachineModules(
            resolvedMetricManager,
            lockManager,
            pendingTransitionsBuilder.build(),
            postTransitionsBuilder.build()))
        .build();
    return component.context();
  }

  private MetricManager resolveMetricManager() {
    if (metricManager != null) {
      return metricManager;
    } else if (metricRegistry != null) {
      return new CodahaleMetricManager(metricRegistry);
    } else {
      return null;
    }
  }

  @Singleton
  @Component(modules = {StateMachineModules.class})
  public interface ContextComponent {

    Context context();

  }

  @Module
  public static class StateMachineModules {

    private final MetricManager metricManager;
    private final LockManager lockManager;
    private final Set<Hook.PendingTransition> pendingTransitions;
    private final Set<Hook.PostTransition> postTransitions;

    public StateMachineModules(final MetricManager metricManager,
                               final LockManager lockManager,
                               final Set<Hook.PendingTransition> pendingTransitions,
                               final Set<Hook.PostTransition> postTransitions) {
      this.metricManager = metricManager;
      this.lockManager = lockManager;
      this.pendingTransitions = pendingTransitions;
      this.postTransitions = postTransitions;
    }

    @Provides
    @Singleton
    @Named("PendingTransition")
    public Set<Hook.PendingTransition> pendingTransitions() {
      return pendingTransitions;
    }

    @Provides
    @Singleton
    @Named("PostTransition")
    public Set<Hook.PostTransition> postTransitions() {
      return postTransitions;
    }

    @Provides
    @Singleton
    public LockManager lockManager() {
      if (lockManager == null) {
        return new NullLockManager();
      } else {
        return lockManager;
      }
    }

    @Provides
    @Singleton
    public MetricManager metricManager() {
      if (metricManager == null) {
        return new NullMetricManager();
      } else {
        return metricManager;
      }
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper(final ObjectMapperFactory factory) {
      return factory.objectMapper();
    }

  }

}
