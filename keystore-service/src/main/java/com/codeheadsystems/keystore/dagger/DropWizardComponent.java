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
import com.codeheadsystems.keystore.resource.JettyResource;
import java.util.Set;

/**
 * Your server needs to implement this component in order to start a drop wizard server.
 */
public interface DropWizardComponent {

  /**
   * Resources set.
   *
   * @return the set
   */
  Set<JettyResource> resources();

  /**
   * Health checks set.
   *
   * @return the set
   */
  Set<HealthCheck> healthChecks();

}
