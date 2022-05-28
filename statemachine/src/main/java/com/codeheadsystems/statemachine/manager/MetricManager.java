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

package com.codeheadsystems.statemachine.manager;

import java.util.function.Supplier;

/**
 * Provide your own metric manager.
 */
public interface MetricManager {

  /**
   * This is effectively a 'rate'. Number/interval.
   *
   * @param metricName to use.
   * @param value      a value. Note, having a zero here is useful for cases
   *                   like error rates to indicate no value.
   */
  void meter(String metricName, long value);

  /**
   * Calls the time method via a runable instead of a supplier.
   *
   * @param metricName to use.
   * @param runnable   to execute.
   * @return basically nothing.
   */
  default Void time(String metricName, Runnable runnable) {
    return time(metricName, () -> {
      runnable.run();
      return null;
    });
  }

  /**
   * Latency of a given method. Note this should include counts too.
   *
   * @param metricName to use.
   * @param supplier   to execute.
   * @param <R>        return type.
   * @return the value from the method.
   */
  <R> R time(String metricName, Supplier<R> supplier);
}
