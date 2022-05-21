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
