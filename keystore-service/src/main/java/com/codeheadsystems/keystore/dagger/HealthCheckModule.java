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

import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.keystore.healthchecks.BasicHealthCheck;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

/**
 * All health checks should be referenced here.
 */
@Module
public interface HealthCheckModule {

  /**
   * Basic health check health check.
   *
   * @param healthCheck the health check
   * @return the health check
   */
  @Binds
  @IntoSet
  HealthCheck basicHealthCheck(final BasicHealthCheck healthCheck);

}
