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

package com.codeheadsystems.keystore.common.helper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bouncycastle.util.encoders.Base64;

/**
 * Helper for managing strings and data types in terrapin.
 */
@Singleton
public class DataHelper {

  private static final Charset CHARSET = StandardCharsets.UTF_16;
  private static final byte CLEAR_BYTE = (byte) 0;

  /**
   * Default constructor.
   */
  @Inject
  public DataHelper() {

  }

  /**
   * Converts the string to bytes using standard charset.
   *
   * @param string to convert.
   * @return bytes. byte [ ]
   */
  public byte[] toByte(final String string) {
    return string.getBytes(CHARSET);
  }

  /**
   * Converts the bytes to the string using the standard charset.
   *
   * @param bytes to convert.
   * @return the resulting string.
   */
  public String toString(final byte[] bytes) {
    return new String(bytes, CHARSET);
  }

  /**
   * Clears the current byte array... replacing everything with zeros.
   *
   * @param bytes array to clear.
   */
  public void clear(final byte[] bytes) {
    Arrays.fill(bytes, CLEAR_BYTE);
  }

  /**
   * Converts the bytearray to a base64 string.
   *
   * @param bytes to convert.
   * @return base 64 string.
   */
  public String toBase64(final byte[] bytes) {
    return Base64.toBase64String(bytes);
  }

  /**
   * Converts the string to a base64 string. Intermediary bytes is cleared out so we don't have to wait for
   * garbage collection.
   *
   * @param regularString to convert.
   * @return base64 string.
   */
  public String toBase64(final String regularString) {
    final byte[] bytes = toByte(regularString);
    final String base64 = toBase64(bytes);
    clear(bytes);
    return base64;
  }

  /**
   * Converts the base64 string to bytes.
   *
   * @param base64 string to convert.
   * @return bytes that were converted.
   */
  public byte[] toBytesFromBase64(final String base64) {
    return Base64.decode(base64);
  }

  /**
   * Converts the base64 string to the original string it was. Intermedia bytes are cleared out.
   *
   * @param base64 string to convert.
   * @return string of the results.
   */
  public String toStringFromBase64(final String base64) {
    final byte[] bytes = toBytesFromBase64(base64);
    final String string = toString(bytes);
    clear(bytes);
    return string;
  }

}
