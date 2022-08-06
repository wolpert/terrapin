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

package com.codeheadsystems.test.datastore;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jUnit (jupiter) pushes us to inheritance model, so creating a 'base class' to add features to
 * all of our datastore extensions we support.
 */
public abstract class DataStoreExtension implements BeforeEachCallback {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataStoreExtension.class);

  protected final ExtensionContext.Namespace namespace;

  protected DataStoreExtension() {
    namespace = ExtensionContext.Namespace.create(namespaceClass());
    LOGGER.info("DataStoreExtension({})", namespace);
  }

  protected abstract Class<?> namespaceClass();

  protected void withStore(final ExtensionContext context,
                           final Consumer<ExtensionContext.Store> consumer) {
    consumer.accept(context.getStore(namespace));
  }

  @Override
  public void beforeEach(final ExtensionContext context) {
    withStore(context, store -> {
      context.getRequiredTestInstances().getAllInstances().forEach(o -> {
        Arrays.stream(o.getClass().getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(DataStore.class))
            .forEach(field -> {
              setValueForField(store, o, field);
            });
      });
    });
  }

  protected void setValueForField(final ExtensionContext.Store store,
                                  final Object o,
                                  final Field field) {
    final Object value = store.get(field.getType()); // Check the store to see we have this type.
    if (value != null) { // Good, go set it.
      LOGGER.info("Setting field {}:{}", field.getName(), field.getType().getSimpleName());
      enableSettingTheField(field);
      try {
        field.set(o, value);
      } catch (IllegalAccessException e) {
        LOGGER.error("Unable to set the field value for {}", field.getName(), e);
        LOGGER.error("Continuing, but expect nothing good will happen next.");
      }
    } else { // Too bad. Fail loudly so the dev can fix it.
      LOGGER.error("Type {} is unknown to the DynamoDB extension. You have the annotation on the wrong field",
          field.getType());
      throw new IllegalArgumentException("Unable to find DynamoDB extension value of type " + field.getType());
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
      LOGGER.error("Unable to change accessibility for field due to private var or security manager: {}",
          field.getName());
      LOGGER.error("The setting will likely fail. Consider changing that type to protected.", re);
    }
  }
}
