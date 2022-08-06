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

package com.codeheadsystems.oop;

import java.util.function.Supplier;

/**
 * Out of process mock provides the ability to define mocks externally to the current process.
 * <p>
 * This exists as a way to do integration testing where you control in the test client what is
 * actually mocked. To use this, you provide the proxy for the closure you want to mock. By
 * default, the mock will simply call the closure being mocked. This is to help prevent setting up
 * mocks in a production environment.
 */
public interface OopMock {

  /**
   * This is the main execution method that everything is based on ways to call it.
   * It will return the results of the supplier unless there is a mock defined in the
   * framework for the class.
   *
   * @param returnClass We use this to cast the result of the mock if we had one.
   * @param supplier    The method that is being mocked.
   * @param lookup      Used to lookup the mock result.
   * @param id          id of the actual request.
   * @param <R>         Type of result.
   * @return the result, mocked or supplied.
   */
  <R> R proxy(Class<R> returnClass, Supplier<R> supplier, String lookup, String id);

}
