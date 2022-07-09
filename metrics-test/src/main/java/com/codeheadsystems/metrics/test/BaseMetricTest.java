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

package com.codeheadsystems.metrics.test;

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.MetricsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class BaseMetricTest {

    protected MetricsHelper metricsHelper;

    abstract protected MetricsHelper metricsHelper();

    @BeforeEach
    void setup() {
        metricsHelper = metricsHelper();
    }

    @Test
    public void basicRun() {
        metricsHelper.with(() -> {
            final Metrics metrics = metricsHelper.get();
            metrics.addDimension("this", "good");
            metrics.count("something", 4);
            metrics.count("something", 1);
            metrics.time("a thing", () -> {
                try {
                    Thread.sleep(222);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        });
    }

}
