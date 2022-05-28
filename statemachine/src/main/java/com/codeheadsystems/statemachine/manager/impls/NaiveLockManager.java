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

package com.codeheadsystems.statemachine.manager.impls;

import com.codeheadsystems.statemachine.manager.LockManager;
import com.codeheadsystems.statemachine.model.StateMachine;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This lock manager is provided as an example of how to make your own lock manager. It is only useful if the
 * state value in the target object is never used by methods outside of the state machine itself, and if no
 * other method uses the target object for synchronization. Use it if you must, but I'm leaving the deprecated
 * annotation on it.
 */
@Singleton
@Deprecated
public class NaiveLockManager implements LockManager {

  @Inject
  public NaiveLockManager() {

  }

  @Override
  public <T> void transitionUnderLock(final StateMachine stateMachine,
                                      final T targetObject,
                                      final Runnable stateChangeMethod) {
    synchronized (targetObject) {
      stateChangeMethod.run();
    }
  }
}
