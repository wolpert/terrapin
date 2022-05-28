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

package com.codeheadsystems.statemachine.model;

import com.codeheadsystems.statemachine.Hook;
import java.lang.reflect.Method;
import java.util.Set;
import org.immutables.value.Value;

/**
 * Provides details for an object's invocation. Not serializable.
 */
@Value.Immutable
public interface InvocationModel<T> {

  /**
   * Provides the property name of the object we want to manage.
   *
   * @return string.
   */
  String propertyName();

  /**
   * The class the object is good for.
   *
   * @return class.
   */
  Class<T> targetClass();

  /**
   * A method to get the property from the object.
   *
   * @return method.
   */
  Method retrieveMethod();

  /**
   * A method to set the property in the object.
   *
   * @return method.
   */
  Method updateMethod();

  /**
   * The set of pending transition hooks to use.
   *
   * @return pending transition hooks.
   */
  Set<Hook.PendingTransition> pendingTransitionHooks();

  /**
   * The set of post transition hooks to use.
   *
   * @return post transition hooks.
   */
  Set<Hook.PostTransition> postTransitionHooks();

  @Value.Derived
  default String retrieveMethodSignature() {
    return targetClass().getCanonicalName() + "." + retrieveMethod().getName();
  }

  @Value.Derived
  default String updateMethodSignature() {
    return targetClass().getCanonicalName() + "," + updateMethod().getName();
  }

}
