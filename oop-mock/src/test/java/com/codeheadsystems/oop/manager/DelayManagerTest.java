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

package com.codeheadsystems.oop.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.codeheadsystems.oop.OopMockConfiguration;
import java.time.Clock;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelayManagerTest {

  public static final long MILLS = 500L;
  @Mock private SleeperManager sleeperManager;
  @Mock private OopMockConfiguration configuration;
  @Mock private Clock clock;

  private DelayManager delayManager;

  private static Stream<Arguments> delayTestData() {
    return Stream.of(
        // max, start, delayWanted, end, whatWeExpect
        Arguments.of(100, 10, 20, 30, 0), // already hit the delay
        Arguments.of(100, 10, 10, 12, 8), // only 8 left to delay
        Arguments.of(100, 30, 20, 30, 20),
        Arguments.of(100, 40, 20, 30, 20), // start is greater than end, so no time spent.
        Arguments.of(100, 10, 40, 30, 20),
        Arguments.of(1000, 100, 100, 100, 100), // verify use-case for disabled.
        Arguments.of(10, 10, 40, 30, 10), // should only wait max time.
        Arguments.of(100, 100, 140, 200, 40) // max isn't confused with actual.
    );
  }

  @ParameterizedTest
  @MethodSource("delayTestData")
  void delay(final long max, final long start, final long delay, final long end, final long expected) {
    when(configuration.delayResponseEnabled()).thenReturn(true);
    when(configuration.maxDelayTimeMS()).thenReturn(max);
    delayManager = new DelayManager(configuration, clock, sleeperManager);
    when(clock.millis()).thenReturn(end);
    delayManager.delay(start, delay);
    verify(sleeperManager).sleep(expected);
  }

  @Test
  void delay_disabled() {
    when(configuration.delayResponseEnabled()).thenReturn(false);
    when(configuration.maxDelayTimeMS()).thenReturn(100L);
    delayManager = new DelayManager(configuration, clock, sleeperManager);
    delayManager.delay(100L, 100L);
    verifyNoInteractions(sleeperManager);
  }

  @Test
  void startMillis() {
    delayManager = new DelayManager(configuration, clock, sleeperManager);
    when(clock.millis()).thenReturn(MILLS);

    final long result = delayManager.startMillis();
    assertThat(result).isEqualTo(MILLS);
  }
}