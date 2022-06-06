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

package com.codeheadsystems.oop.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SleeperManagerTest {

    private SleeperManager sleeperManager;

    @BeforeEach
    void setup() {
        sleeperManager = new SleeperManager();
    }

    @Test
    void sleep() {
        sleeperManager.sleep(0);// no exception please.
    }

    /**
     * I wait for NO cpu. Well, maybe a little.
     *
     * @throws InterruptedException
     */
    @Test
    void interruptedSleep() throws InterruptedException {
        final Thread currentThread = Thread.currentThread();
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        final Runnable runnable = () -> {
            try {
                latch.await();
                Thread.sleep(10L); // Roll dem bones
                currentThread.interrupt();
            } catch (Throwable e) {
                thrown.set(e);
            }
        };
        Thread t = new Thread(runnable);
        t.start();
        latch.countDown();
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> sleeperManager.sleep(1000L));
        assertThat(thrown.get())
                .isNull();
    }
}