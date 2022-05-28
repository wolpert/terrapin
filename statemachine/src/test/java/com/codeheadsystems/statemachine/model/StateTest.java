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

import com.codeheadsystems.test.model.BaseJacksonTest;

/**
 * Purpose:
 */
class StateTest extends BaseJacksonTest<State> {

  @Override
  protected Class<State> getBaseClass() {
    return State.class;
  }

  @Override
  protected State getInstance() {
    return ImmutableState.builder()
        .name("name")
        .putTransitions("t1", ImmutableTransition.builder().name("t1").nextState("s1").build())
        .putTransitions("21", ImmutableTransition.builder().name("t2").nextState("s1").build())
        .build();
  }


}