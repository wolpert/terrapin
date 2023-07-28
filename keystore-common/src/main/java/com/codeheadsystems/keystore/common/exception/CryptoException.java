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

package com.codeheadsystems.keystore.common.exception;

/**
 * Gives us details on crypto exceptions.
 */
public class CryptoException extends Exception {
  /**
   * Instantiates a new Crypto exception.
   */
  public CryptoException() {
    super();
  }

  /**
   * Instantiates a new Crypto exception.
   *
   * @param message the message
   */
  public CryptoException(final String message) {
    super(message);
  }

  /**
   * Instantiates a new Crypto exception.
   *
   * @param message the message
   * @param cause   the cause
   */
  public CryptoException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new Crypto exception.
   *
   * @param cause the cause
   */
  public CryptoException(final Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new Crypto exception.
   *
   * @param message            the message
   * @param cause              the cause
   * @param enableSuppression  the enable suppression
   * @param writableStackTrace the writable stack trace
   */
  protected CryptoException(final String message,
                            final Throwable cause,
                            final boolean enableSuppression,
                            final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
