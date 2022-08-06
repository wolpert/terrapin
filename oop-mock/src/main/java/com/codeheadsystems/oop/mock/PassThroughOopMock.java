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

package com.codeheadsystems.oop.mock;

import com.codeheadsystems.oop.OopMock;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Pass through oop mock. The default response. This is forced into a singleton since it's likely
 * highly shared.
 */
@Singleton
public class PassThroughOopMock implements OopMock {

  @Inject
  public PassThroughOopMock() {

  }

  @Override
  public <R> R proxy(Class<R> returnClass, Supplier<R> supplier, String lookup, String id) {
    return supplier.get();
  }

  @Override
  public String toString() {
    return "PassThroughOopMock{} (OopMock disabled)";
  }
}
