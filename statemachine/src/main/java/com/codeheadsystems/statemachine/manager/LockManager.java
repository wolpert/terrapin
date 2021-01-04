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
