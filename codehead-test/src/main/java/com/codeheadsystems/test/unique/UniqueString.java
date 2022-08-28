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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Provides the annotation for a unique string. Set the value to the prefix.
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
public @interface UniqueString {
  /**
   * The value of the unique string prefix.
   *
   * @return by default, the word unique.
   */
  String prefix() default "unique";

  /**
   * The separator to use between unique components.
   *
   * @return by default, '_'
   */
  String separator() default "_";
}
