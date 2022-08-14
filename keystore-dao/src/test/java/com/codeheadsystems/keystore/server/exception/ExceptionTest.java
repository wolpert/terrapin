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

package com.codeheadsystems.keystore.server.exception;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Just want to make sure all the constructors are setup for these exceptions.
 */
class ExceptionTest {
  private static final String MESSAGE = "a message";
  private static final Exception CAUSE = new Exception();

  private static Stream<Arguments> runtimeExceptions() {
    return Stream.of(
        Arguments.of(DatalayerException.class),
        Arguments.of(DependencyException.class),
        Arguments.of(RetryableException.class)
    );
  }

  @ParameterizedTest
  @MethodSource("runtimeExceptions")
  public <T extends RuntimeException> void testConstructorsForRuntimeExceptions(final Class<T> clazz)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    clazz.getConstructor().newInstance();
    clazz.getConstructor(String.class).newInstance(MESSAGE);
    clazz.getConstructor(Throwable.class).newInstance(CAUSE);
    clazz.getConstructor(String.class, Throwable.class).newInstance(MESSAGE, CAUSE);
  }

}