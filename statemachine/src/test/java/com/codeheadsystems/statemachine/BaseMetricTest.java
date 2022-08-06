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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codeheadsystems.statemachine.manager.MetricManager;
import com.codeheadsystems.statemachine.manager.impls.CodahaleMetricManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

/**
 * This is used to create metrics needed for all tests.
 */
public class BaseMetricTest {

  protected static MetricRegistry metricRegistry;
  protected static Slf4jReporter reporter;
  protected MetricManager metricManager;

  @BeforeAll
  public static void setMetricRegistry() {
    metricRegistry = new MetricRegistry();
    reporter = Slf4jReporter.forRegistry(metricRegistry)
        .outputTo(LoggerFactory.getLogger(CodahaleMetricManager.class))
        .convertRatesTo(SECONDS)
        .convertDurationsTo(MILLISECONDS)
        .build();
    reporter.start(60, SECONDS);
  }

  @AfterAll
  public static void afterEverything() {
    reporter.close();
  }

  @BeforeEach
  public void setupMetricManager() {
    metricManager = new CodahaleMetricManager(metricRegistry);
  }


}
