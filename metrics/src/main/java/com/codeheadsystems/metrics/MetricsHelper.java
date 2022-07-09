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

package com.codeheadsystems.metrics;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * MetricsHelper is a thread-safe class that gives you the ability for a top-level method to call the 'with' method
 * which sets the metrics in the helper instance, which is retrievable by the 'get' method. This way, you do not
 * have to add the metrics object to every method name that you want to share.
 * <p>
 * If you make a mistake, and there is no metrics set, then the metrics helper will return a null metrics.
 */
public interface MetricsHelper {

    <R> R with(final Function<Metrics, R> function);

    default void with(final Runnable runnable) {
        with(m -> {
            runnable.run();
        });
    }

    default void with(final Consumer<Metrics> consumer) {
        with(m -> {
            consumer.accept(m);
            return null;
        });
    }

    Metrics get();
}
