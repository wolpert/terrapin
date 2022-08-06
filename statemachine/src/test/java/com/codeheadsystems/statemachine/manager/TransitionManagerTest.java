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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.codeheadsystems.statemachine.BaseMetricTest;
import com.codeheadsystems.statemachine.Hook;
import com.codeheadsystems.statemachine.exceptions.TransitionException;
import com.codeheadsystems.statemachine.manager.impls.NullLockManager;
import com.codeheadsystems.statemachine.model.ActiveStateMachine;
import com.codeheadsystems.statemachine.model.ImmutableState;
import com.codeheadsystems.statemachine.model.ImmutableStateMachine;
import com.codeheadsystems.statemachine.model.ImmutableTransition;
import com.codeheadsystems.statemachine.model.InvocationModel;
import com.codeheadsystems.statemachine.model.StateMachine;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransitionManagerTest extends BaseMetricTest {

  public static final String FIRST_STATE = "s1";
  public static final String SECOND_STATE = "s2";
  public static final String TRANSITION = "t";
  private static final StateMachine MACHINE = ImmutableStateMachine.builder()
      .name("name")
      .version(1L)
      .id("id")
      .putStates(FIRST_STATE, ImmutableState.builder()
          .name(FIRST_STATE)
          .putTransitions(TRANSITION, ImmutableTransition.builder()
              .name(TRANSITION)
              .nextState(SECOND_STATE)
              .build())
          .build())
      .putStates(SECOND_STATE, ImmutableState.builder().name(SECOND_STATE).build())
      .build();

  @Mock
  private ActiveStateMachine<SampleClass> activeStateMachine;
  @Mock
  private InvocationManager invocationManager;
  @Mock
  private InvocationModel<SampleClass> model;
  @Mock
  private Hook.PendingTransition pendingTransition;
  @Mock
  private Hook.PostTransition postTransition;
  @Captor
  private ArgumentCaptor<String> stringArgumentCaptor;

  private TransitionManager transitionManager;
  private LockManager lockManager;

  @BeforeEach
  void setUp() {
    lockManager = new NullLockManager();
    transitionManager = new TransitionManager(invocationManager, metricManager, lockManager);
  }

  @Test
  void transitions_missingStateName() {
    final SampleClass object = new SampleClass(FIRST_STATE);
    when(activeStateMachine.stateMachine()).thenReturn(MACHINE);
    when(activeStateMachine.invocationModel()).thenReturn(model);
    when(invocationManager.get(model, object)).thenReturn(null);
    assertThatExceptionOfType(TransitionException.class)
        .isThrownBy(() -> transitionManager.transitions(activeStateMachine, object));
  }

  @Test
  void transitions_badStateName() {
    final SampleClass object = new SampleClass(FIRST_STATE);
    when(activeStateMachine.stateMachine()).thenReturn(MACHINE);
    when(activeStateMachine.invocationModel()).thenReturn(model);
    when(invocationManager.get(model, object)).thenReturn("null");
    assertThatExceptionOfType(TransitionException.class)
        .isThrownBy(() -> transitionManager.transitions(activeStateMachine, object));
  }

  @Test
  void transitions_oneTransition() {
    final SampleClass object = new SampleClass(FIRST_STATE);
    when(activeStateMachine.stateMachine()).thenReturn(MACHINE);
    when(activeStateMachine.invocationModel()).thenReturn(model);
    when(invocationManager.get(model, object)).thenReturn(FIRST_STATE);
    assertThat(transitionManager.transitions(activeStateMachine, object))
        .isNotNull()
        .hasSize(1)
        .contains(TRANSITION);
  }

  @Test
  void transitions_noTransition() {
    final SampleClass object = new SampleClass(SECOND_STATE);
    when(activeStateMachine.stateMachine()).thenReturn(MACHINE);
    when(activeStateMachine.invocationModel()).thenReturn(model);
    when(invocationManager.get(model, object)).thenReturn(SECOND_STATE);
    assertThat(transitionManager.transitions(activeStateMachine, object))
        .isNotNull()
        .isEmpty();
  }

  @Test
  void transition() {
    final SampleClass object = new SampleClass(FIRST_STATE);
    when(invocationManager.get(model, object)).thenReturn(FIRST_STATE);
    when(model.pendingTransitionHooks()).thenReturn(ImmutableSet.of(pendingTransition));
    when(model.postTransitionHooks()).thenReturn(ImmutableSet.of(postTransition));

    final SampleClass result = transitionManager.transition(MACHINE, model, object, TRANSITION);

    assertThat(result)
        .isEqualTo(object);
    InOrder inOrder = Mockito.inOrder(pendingTransition, invocationManager, postTransition);
    inOrder.verify(pendingTransition).transition(object, TRANSITION);
    inOrder.verify(invocationManager).set(eq(model), eq(object), stringArgumentCaptor.capture());
    inOrder.verify(postTransition).transition(object, TRANSITION);
    assertThat(stringArgumentCaptor.getValue())
        .isEqualTo(SECOND_STATE);
  }

  @Test
  void transition_badRequest() {
    final SampleClass object = new SampleClass(SECOND_STATE);
    when(invocationManager.get(model, object)).thenReturn(SECOND_STATE);

    assertThatExceptionOfType(TransitionException.class)
        .isThrownBy(() -> transitionManager.transition(MACHINE, model, object, TRANSITION));
  }

  static class SampleClass {

    private String state;

    public SampleClass(final String state) {
      setState(state);
    }

    public String getState() {
      return state;
    }

    public void setState(final String state) {
      this.state = state;
    }

    private String internalGetState() {
      return state;
    }

    public void badSetState(final String state) {
      throw new RuntimeException("blah");
    }

  }
}