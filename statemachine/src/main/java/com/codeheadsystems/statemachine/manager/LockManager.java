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

import com.codeheadsystems.statemachine.model.StateMachine;

/**
 * Define the lock manager to use when transitioning a object from one state to another. This is only used
 * to wrap the calls to 'get/set' methods on the stateful object during the transition.
 */
public interface LockManager {

  /**
   * Invokes the state change in the transitionManager for the target object.
   *
   * @param stateMachine      which involves the object being executed.
   * @param targetObject      that needs the state change.
   * @param stateChangeMethod the transitionManager call that will do the work.
   * @param <T>               type of object being impacted.
   */
  <T> void transitionUnderLock(final StateMachine stateMachine,
                               final T targetObject,
                               final Runnable stateChangeMethod);

}
