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

package com.codeheadsystems.statemachine.manager;

import static com.codahale.metrics.MetricRegistry.name;

import com.codeheadsystems.statemachine.exceptions.TargetException;
import com.codeheadsystems.statemachine.model.InvocationModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides a way to manage object invocations via the invocation model. Internally used.
 */
@Singleton
public class InvocationManager {

  private final MetricManager metricManager;

  @Inject
  public InvocationManager(final MetricManager metricManager) {
    this.metricManager = metricManager;
  }

  /**
   * Sets the string value in the object.
   *
   * @param model  that has the methods.
   * @param target object we are setting the methods for.
   * @param value  we want.
   * @param <T>    type of object.
   */
  public <T> void set(final InvocationModel<T> model,
                      final T target,
                      final String value) {
    metricManager.time(name(InvocationManager.class, model.updateMethodSignature()), () ->
        invoke(model.updateMethod(), target, value));
  }

  /**
   * Gets the string value in the object. Can be null.
   *
   * @param model  that has the methods.
   * @param target object we are getting the methods for.
   * @param <T>    type of object.
   */
  public <T> String get(final InvocationModel<T> model,
                        final T target) {
    final Object result = metricManager.time(name(InvocationManager.class, model.retrieveMethodSignature()), () ->
        invoke(model.retrieveMethod(), target));
    if (result == null) {
      return null;
    } else {
      return result.toString();
    }
  }

  private Object invoke(final Method method, final Object target, final Object... params) {
    try {
      return method.invoke(target, params);
    } catch (InvocationTargetException e) {
      throw new TargetException(String.format("Target Exception %s, %s", method.getName(), target), e);
    } catch (IllegalAccessException | IllegalArgumentException e) {
      throw new IllegalArgumentException(String.format("Bad method for target :%s(%s):%s", target.getClass().getName(), method.getName(), target), e);
    }
  }

}
