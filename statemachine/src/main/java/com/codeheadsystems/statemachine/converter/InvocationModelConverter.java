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

package com.codeheadsystems.statemachine.converter;

import static com.codeheadsystems.statemachine.Hook.PendingTransition;
import static com.codeheadsystems.statemachine.Hook.PostTransition;

import com.codeheadsystems.statemachine.annotation.StateTarget;
import com.codeheadsystems.statemachine.model.ImmutableInvocationModel;
import com.codeheadsystems.statemachine.model.InvocationModel;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts a class that contains state information into an Invocation model converter.
 */
@Singleton
public class InvocationModelConverter {

  private static final Logger log = LoggerFactory.getLogger(InvocationModelConverter.class);

  private final Set<PendingTransition> globalPendingTransitions;
  private final Set<PostTransition> globalPostTransitions;

  @Inject
  public InvocationModelConverter(@Named("PendingTransition") final Set<PendingTransition> globalPendingTransitions,
                                  @Named("PostTransition") final Set<PostTransition> globalPostTransitions) {
    this.globalPendingTransitions = globalPendingTransitions;
    this.globalPostTransitions = globalPostTransitions;
    log.debug("InvocationModelConverter");
  }

  /**
   * Generates an invocation model for the given class. Must use the default getState()/setState() pattern
   * or use annotations.
   *
   * @param targetClass class we use.
   * @param <T>         type for return.
   * @return a usable model.
   */
  public <T> InvocationModel<T> generate(final Class<T> targetClass) {
    log.debug("generate({})", targetClass);
    return generateFromField(targetClass)
        .orElseGet(() -> generate(targetClass, "state"));
  }

  // TODO alow for the field name to be set from the annotation. Requires handling the getter/setter based on
  // the field instead of the annotation.
  private <T> Optional<InvocationModel<T>> generateFromField(final Class<T> targetClass) {
    return Arrays.stream(targetClass.getDeclaredFields())
        .filter(this::isAnnotationPresent)
        .findFirst()
        .map(Field::getName)
        .map(name -> generate(targetClass, name));
  }

  private boolean isAnnotationPresent(final Field field) {
    field.setAccessible(true);
    return field.isAnnotationPresent(StateTarget.class);
  }

  /**
   * Generates an invocation model for a given class. Must follow the bean access pattern.
   *
   * @param targetClass  Class we use.
   * @param propertyName name of the property.
   * @param <T>          type of the class.
   * @return a usable model.
   */
  public <T> InvocationModel<T> generate(final Class<T> targetClass,
                                         final String propertyName) {
    log.debug("generate({}),{}", targetClass, propertyName);
    final String name = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    return generate(targetClass, propertyName, "get" + name, "set" + name);
  }

  /**
   * Generates an invocation model for a given class. Must follow the bean access pattern.
   *
   * @param targetClass  Class we use.
   * @param propertyName name of the property.
   * @param <T>          type of the class.
   * @return a usable model.
   */
  public <T> InvocationModel<T> generate(final Class<T> targetClass,
                                         final String propertyName,
                                         final String getMethodName,
                                         final String setMethodName) {
    log.debug("generate({}),{},{},{}", targetClass, propertyName, getMethodName, setMethodName);
    return generate(targetClass, propertyName,
        getMethod(targetClass, getMethodName),
        getMethod(targetClass, setMethodName, String.class));
  }

  public <T> InvocationModel<T> generate(final Class<T> targetClass,
                                         final String propertyName,
                                         final Method getMethod,
                                         final Method setMethod) {
    log.debug("generate({}),{},{},{}", targetClass, propertyName, getMethod.getName(), setMethod.getName());
    return ImmutableInvocationModel.<T>builder()
        .targetClass(targetClass)
        .propertyName(propertyName)
        .retrieveMethod(getMethod)
        .updateMethod(setMethod)
        .addAllPendingTransitionHooks(globalPendingTransitions)
        .addAllPostTransitionHooks(globalPostTransitions)
        .build();
  }

  private Method getMethod(Class<?> targetClass, String methodName, Class<?>... params) {
    try {
      return targetClass.getMethod(methodName, params);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("No method available: " + targetClass.getName() + ":" + methodName + ":" + Arrays.toString(params), e);
    }
  }
}
