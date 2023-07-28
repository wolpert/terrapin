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

/**
 * This type of exception is thrown when a downstream dependency is failing, and we cannot recover.
 */
public class DependencyException extends RuntimeException {

  /**
   * Instantiates a new Dependency exception.
   */
  public DependencyException() {
    super();
  }

  /**
   * Instantiates a new Dependency exception.
   *
   * @param message the message
   */
  public DependencyException(final String message) {
    super(message);
  }

  /**
   * Instantiates a new Dependency exception.
   *
   * @param message the message
   * @param cause   the cause
   */
  public DependencyException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new Dependency exception.
   *
   * @param cause the cause
   */
  public DependencyException(final Throwable cause) {
    super(cause);
  }
}
