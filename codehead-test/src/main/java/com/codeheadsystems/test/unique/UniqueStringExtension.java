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

package com.codeheadsystems.test.unique;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Provides for a unique string that is valid for the tests.
 */
public class UniqueStringExtension implements BeforeEachCallback, BeforeAllCallback {
  public static final String ATOMIC = "atomic";
  protected final ExtensionContext.Namespace namespace;

  /**
   * Default constructor.
   */
  public UniqueStringExtension() {
    namespace = ExtensionContext.Namespace.create(UniqueStringExtension.class);
  }

  /**
   * Handles the beforeAll case. Setting up the context.
   *
   * @param context the current extension context; never {@code null}
   * @throws Exception if anything goes wrong.
   */
  @Override
  public void beforeAll(final ExtensionContext context) throws Exception {
    final ExtensionContext.Store store = context.getStore(namespace);
    store.put(ATOMIC, new AtomicInteger(0));
  }

  /**
   * Handles the before each case. Setting the vars in the object.
   *
   * @param context the current extension context; never {@code null}
   * @throws Exception if anything goes wrong.
   */
  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    final ExtensionContext.Store store = context.getStore(namespace);
    final AtomicInteger atomic = store.get(ATOMIC, AtomicInteger.class);
    final long currentTime = System.currentTimeMillis();
    final int currentRun = atomic.incrementAndGet();
    context.getRequiredTestInstances().getAllInstances()
        .forEach(instance -> uniqueFields(instance.getClass())
            .forEach(field -> {
              enableSettingTheField(field);
              final UniqueString prefixAnnotation = field.getAnnotation(UniqueString.class);
              final String sep = prefixAnnotation.separator();
              final String value = prefixAnnotation.prefix() + sep + currentTime + sep + currentRun;
              setValueForField(value, instance, field);
            }));
  }

  public Set<Field> uniqueFields(Class<?> clazz) {
    final HashSet<Field> set = new HashSet<>();
    while (clazz != Object.class) {
      Arrays.stream(clazz.getDeclaredFields())
          .filter(f -> f.isAnnotationPresent(UniqueString.class))
          .forEach(set::add);
      clazz = clazz.getSuperclass();
    }
    return set;
  }

  /**
   * Sets the value for the field. Really this just ignores the exception.
   *
   * @param value  to set.
   * @param object on this object.
   * @param field  in this field.
   */
  protected void setValueForField(final String value,
                                  final Object object,
                                  final Field field) {
    try {
      field.set(object, value);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * This allows us to set the field directly. It will fail if the security manager in play disallows it.
   * We can talk about justifications all we want, but really we know Java is not Smalltalk. Meta programming
   * is limited here. So... we try to do the right thing.
   *
   * @param field to change accessibility for.
   */
  protected void enableSettingTheField(final Field field) {
    try {
      field.setAccessible(true);
    } catch (RuntimeException re) {
      re.printStackTrace();
    }
  }
}
