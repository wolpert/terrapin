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
 * This type of exception is thrown if data from the service to the data store is actually bad. This happens
 * if someone manually updated data in the database, or if the database became corrupt.
 */
public class DatalayerException extends RuntimeException {

  public DatalayerException() {
    super();
  }

  public DatalayerException(final String message) {
    super(message);
  }

  public DatalayerException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public DatalayerException(final Throwable cause) {
    super(cause);
  }
}
