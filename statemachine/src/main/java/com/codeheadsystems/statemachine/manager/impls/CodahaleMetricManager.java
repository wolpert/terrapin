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

package com.codeheadsystems.statemachine.manager.impls;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codeheadsystems.statemachine.manager.MetricManager;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CodahaleMetricManager implements MetricManager {

  private final MetricRegistry metricRegistry;

  @Inject
  public CodahaleMetricManager(final MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
  }

  @Override
  public void meter(final String metricName, final long value) {
    metricRegistry.meter(metricName).mark(value);
  }

  /**
   * The timer is used for this method. It provides the latency and a rate. We
   * add a failure counter to the method to track runtime exceptions.
   *
   * @param metricName to use.
   * @param supplier   to execute.
   * @param <R>        return type.
   * @return the value from the method.
   */
  @Override
  public <R> R time(final String metricName, final Supplier<R> supplier) {
    final Timer timer = metricRegistry.timer(metricName);
    final Meter failure = metricRegistry.meter(name(metricName, "failure"));
    failure.mark(0); // set the count if needed.
    final Timer.Context context = timer.time();
    try {
      return supplier.get();
    } catch (RuntimeException re) {
      failure.mark();
      throw re;
    } finally {
      context.stop();
    }
  }

}
