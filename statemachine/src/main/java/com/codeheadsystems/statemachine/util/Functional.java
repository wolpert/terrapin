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

package com.codeheadsystems.statemachine.util;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Static methods to help with functional programming.
 */
public class Functional {

  private Functional() {

  }

  /**
   * Immutable map helper.... adds the key/value to the immutable map.
   *
   * @param map   we are starting with
   * @param key   of the entry.
   * @param value of the entry.
   * @param <K>   key type.
   * @param <V>   value type.
   * @return new map with the augmented entries.
   */
  public static <K, V> Map<K, V> add(final Map<K, V> map, final K key, final V value) {
    if (map.containsKey(key)) {
      final Map<K, V> growingMap = new HashMap<>(map);
      growingMap.put(key, value);
      return ImmutableMap.copyOf(growingMap);
    } else {
      return ImmutableMap.<K, V>builder().putAll(map).put(key, value).build();
    }
  }

  public static <T> boolean is(final Optional<T> optionalT, final T value) {
    if (!optionalT.isPresent() || value == null) {
      return false;
    }
    return value.equals(optionalT.get());
  }

}
