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

import com.codeheadsystems.statemachine.BaseMetricTest;
import com.codeheadsystems.statemachine.converter.InvocationModelConverter;
import com.codeheadsystems.statemachine.manager.InvocationManager;
import com.codeheadsystems.statemachine.manager.LockManager;
import com.codeheadsystems.statemachine.manager.TransitionManager;
import com.codeheadsystems.statemachine.manager.impls.NullLockManager;
import com.codeheadsystems.statemachine.model.ImmutableState;
import com.codeheadsystems.statemachine.model.ImmutableStateMachine;
import com.codeheadsystems.statemachine.model.ImmutableTransition;
import com.codeheadsystems.statemachine.model.InvocationModel;
import com.codeheadsystems.statemachine.model.StateMachine;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransitionManagerFunctionalTest extends BaseMetricTest {

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

  private InvocationModelConverter converter;
  private TransitionManager transitionManager;
  private LockManager lockManager;
  private HookStruct hookStruct;

  @BeforeEach
  void setUp() {
    hookStruct = new HookStruct();
    lockManager = new NullLockManager();
    converter = new InvocationModelConverter(ImmutableSet.of(hookStruct::preTrans), ImmutableSet.of(hookStruct::postTrans));
    transitionManager = new TransitionManager(
        new InvocationManager(metricManager),
        metricManager, lockManager);
  }

  @Test
  void transition() {
    final SampleClass object = new SampleClass(FIRST_STATE);
    final InvocationModel<SampleClass> model = converter.generate(SampleClass.class);

    final SampleClass result = transitionManager.transition(MACHINE, model, object, TRANSITION);

    assertThat(result)
        .isEqualTo(object)
        .hasFieldOrPropertyWithValue("state", SECOND_STATE);
    assertThat(hookStruct)
        .hasFieldOrPropertyWithValue("preTransitionObject", object)
        .hasFieldOrPropertyWithValue("postTransitionObject", object)
        .hasFieldOrPropertyWithValue("preTransitionTransition", TRANSITION)
        .hasFieldOrPropertyWithValue("postTransitionTransition", TRANSITION);
  }

  static public class SampleClass {

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

  static public class HookStruct {
    public Object preTransitionObject;
    public String preTransitionTransition;
    public Object postTransitionObject;
    public String postTransitionTransition;

    public void preTrans(final Object object, final String trans) {
      preTransitionObject = object;
      preTransitionTransition = trans;
    }

    public void postTrans(final Object object, final String trans) {
      postTransitionObject = object;
      postTransitionTransition = trans;
    }
  }
}