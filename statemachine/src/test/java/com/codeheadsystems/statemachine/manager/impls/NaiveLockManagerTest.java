package com.codeheadsystems.statemachine.manager.impls;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.statemachine.manager.LockManager;
import com.codeheadsystems.statemachine.model.StateMachine;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NaiveLockManagerTest {

    @Mock private StateMachine stateMachine;

    private LockManager lockManager;
    private AtomicInteger atomicInteger;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        lockManager = new NaiveLockManager();
        executor = Executors.newFixedThreadPool(10);
    }

    @AfterEach
    void cleanup() {
        executor.shutdownNow();
    }

    // Warning, this doesn't really test the locking... just the execution.
    @Test
    void transitionUnderLock() throws ExecutionException, InterruptedException {
        atomicInteger = new AtomicInteger(0);
        final Future<?> future = executor.submit(() -> { // set to 1 after 1 sec
            lockManager.transitionUnderLock(stateMachine, atomicInteger, () -> {
                atomicInteger.set(1);
            });
        });
        future.get();
        assertThat(atomicInteger)
            .hasValue(1);
    }


}