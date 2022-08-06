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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codeheadsystems.statemachine.manager.impls.CodahaleMetricManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricManagerTest {

  private static final String NAME = "name";

  @Mock
  private MetricRegistry metricRegistry;
  @Mock
  private Timer timer;
  @Mock
  private Meter meter;
  @Mock
  private Meter meterSuccess;
  @Mock
  private Meter meterFailure;
  @Mock
  private Timer.Context context;

  private MetricManager metricManager;

  @BeforeEach
  void setUp() {
    metricManager = new CodahaleMetricManager(metricRegistry);
  }

  @Test
  public void meter() {
    when(metricRegistry.meter(NAME)).thenReturn(meter);

    metricManager.meter(NAME, 10L);

    verify(meter).mark(10L);
  }

  @Test
  public void timeWithSupplier_success() {
    when(metricRegistry.timer(NAME)).thenReturn(timer);
    when(metricRegistry.meter(NAME + ".failure")).thenReturn(meter);
    when(timer.time()).thenReturn(context);

    final Boolean result = metricManager.time(NAME, () -> Boolean.TRUE);

    assertThat(result).isNotNull().isTrue();

    verify(meter).mark(0);
    verify(meter, never()).mark();
    verify(context).stop();
  }

  @Test
  public void timeWithSupplier_fail() {
    when(metricRegistry.timer(NAME)).thenReturn(timer);
    when(metricRegistry.meter(NAME + ".failure")).thenReturn(meter);
    when(timer.time()).thenReturn(context);

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> metricManager.time(NAME, () -> {
          if (true) {
            throw new IllegalArgumentException();
          } else {
            return Boolean.FALSE;
          }
        }));

    verify(meter).mark(0);
    verify(meter).mark();
    verify(context).stop();
  }
}