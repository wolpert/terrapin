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

package com.codeheadsystems.statemachine.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.test.model.BaseJacksonTest;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Purpose:
 */
class StateMachineTest extends BaseJacksonTest<StateMachine> {

  public static final String FIRST_STATE = "s1";
  public static final String SECOND_STATE = "s2";
  public static final String TRANSITION = "t";
  private static final StateMachine MACHINE = ImmutableStateMachine.builder()
      .name("name")
      .version(1L)
      .id("id")
      .initialState(FIRST_STATE)
      .putStates(FIRST_STATE, ImmutableState.builder()
          .name(FIRST_STATE)
          .putTransitions(TRANSITION, ImmutableTransition.builder()
              .name(TRANSITION)
              .nextState(SECOND_STATE)
              .build())
          .build())
      .putStates(SECOND_STATE, ImmutableState.builder().name(SECOND_STATE).build())
      .build();

  @Override
  protected Class<StateMachine> getBaseClass() {
    return StateMachine.class;
  }

  @Override
  protected StateMachine getInstance() {
    return ImmutableStateMachine.builder()
        .id("id")
        .name("name")
        .initialState("s1")
        .version(3L)
        .putStates("s1", ImmutableState.builder().name("s1").build())
        .putStates("s2", ImmutableState.builder().name("s2").build())
        .build();
  }


  @Test
  void getNextStateName() {
    final Optional<String> result = MACHINE.nextState(FIRST_STATE, TRANSITION);

    assertThat(result)
        .isPresent()
        .contains(SECOND_STATE);
  }

  @Test
  void getNextStateName_noTransition() {
    final Optional<String> result = MACHINE.nextState(SECOND_STATE, TRANSITION);

    assertThat(result)
        .isNotPresent();
  }

  @Test
  void transition_badState() {
    final Optional<String> result = MACHINE.nextState(TRANSITION, TRANSITION);

    assertThat(result)
        .isNotPresent();
  }

  @Test
  void transition_badStateInTransition() {
    final StateMachine bad = ImmutableStateMachine.builder()
        .name("name")
        .version(1L)
        .id("id")
        .putStates(FIRST_STATE, ImmutableState.builder()
            .name(FIRST_STATE)
            .putTransitions(TRANSITION, ImmutableTransition.builder()
                .name(TRANSITION)
                .nextState(TRANSITION)
                .build())
            .build())
        .putStates(SECOND_STATE, ImmutableState.builder().name(SECOND_STATE).build())
        .build();

    final Optional<String> result = bad.nextState(FIRST_STATE, TRANSITION);

    assertThat(result)
        .isNotPresent();
  }
}