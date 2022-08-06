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

package com.codeheadsystems.statemachine.functional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.statemachine.BaseMetricTest;
import com.codeheadsystems.statemachine.Context;
import com.codeheadsystems.statemachine.annotation.StateMachineTarget;
import com.codeheadsystems.statemachine.exceptions.TransitionException;
import com.codeheadsystems.statemachine.manager.LockManager;
import com.codeheadsystems.statemachine.manager.MetricManager;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ContextFunctionalTest extends BaseMetricTest {

  public static final String SIMPLE_STATE_MACHINE = "simpleStateMachine.json";

  @Mock private LockManager lockManager;
  @Mock private MetricManager metricManager;

  private Context context;

  @BeforeEach
  void setup() {
    context = Context.builder()
        .metricManager(metricRegistry)
        .build();
  }

  @Test
  void testBuild_defaults() {
    context = Context.builder().build();
    context = Context.builder()
        .lockManager(lockManager)
        .metricManager(metricManager)
        .pendingTransitions(ImmutableSet.of())
        .postTransitions(ImmutableSet.of())
        .build();
  }

  @Test
  void testBasicUseCase() {
    context.register(SimpleTarget.class);
    assertThat(context.isRegistered(SimpleTarget.class)).isTrue();

    final SimpleTarget target = new SimpleTarget();
    assertThatExceptionOfType(TransitionException.class)
        .isThrownBy(() -> context.canTransition(target, "t"));

    context.setInitialState(target);
    assertThat(target)
        .hasFieldOrPropertyWithValue("state", "s1");
    assertThat(context.transitions(target))
        .hasSize(1)
        .contains("t");
    assertThat(context.canTransition(target, "t")).isTrue();

    context.transition(target, "t");
    assertThat(target)
        .hasFieldOrPropertyWithValue("state", "s2");
    assertThat(context.transitions(target))
        .hasSize(0);
    assertThat(context.canTransition(target, "t")).isFalse();

    assertThatExceptionOfType(TransitionException.class)
        .isThrownBy(() -> context.transition(target, "t"));
  }

  @Test
  void testNextStateUseCase() {
    context.register(SimpleTarget.class);
    final SimpleTarget target = new SimpleTarget();
    context.setInitialState(target);
    assertThat(target)
        .hasFieldOrPropertyWithValue("state", "s1");

    assertThat(context.nextState(target)).isTrue();
    assertThat(target)
        .hasFieldOrPropertyWithValue("state", "s2");

    assertThat(context.nextState(target)).isFalse();

  }

  @StateMachineTarget(SIMPLE_STATE_MACHINE)
  public static class SimpleTarget {

    private String state;

    public String getState() {
      return state;
    }

    public void setState(final String state) {
      this.state = state;
    }
  }
}
