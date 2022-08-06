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