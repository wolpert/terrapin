package com.codeheadsystems.statemachine.manager.impls;

import com.codeheadsystems.statemachine.manager.LockManager;
import com.codeheadsystems.statemachine.model.StateMachine;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NullLockManager implements LockManager {

    @Inject
    public NullLockManager() {

    }

    @Override
    public <T> void transitionUnderLock(final StateMachine stateMachine, final T targetObject, final Runnable stateChangeMethod) {
        stateChangeMethod.run();
    }
}
