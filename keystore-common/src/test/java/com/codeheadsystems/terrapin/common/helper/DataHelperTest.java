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

package com.codeheadsystems.terrapin.common.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataHelperTest {

  private static final String TEST_KNOWN_DATA = "This is not a test 78493242342 .fdlsaf 4$$#5432\nfdsaf5";

  private DataHelper helper;
  private Random random;

  @BeforeEach
  void setUp() {
    helper = new DataHelper();
    random = new Random();
  }

  @Test
  void toByteAndToString() {
    final byte[] bytes = helper.toByte(TEST_KNOWN_DATA);
    final String result = helper.toString(bytes);
    assertThat(result)
        .isNotEmpty()
        .isEqualTo(TEST_KNOWN_DATA);
  }

  @Test
  void clear() {
    final byte[] bytes = {0x1, 0x2, 0x3, 0x4, 0x5, 0x6};
    assertThat(bytes)
        .doesNotContain((byte) 0);
    helper.clear(bytes);
    assertThat(bytes)
        .containsOnly((byte) 0);
  }

  @Test
  void base64RoundTrip_string() {
    final String base64 = helper.toBase64(TEST_KNOWN_DATA);
    assertThat(base64)
        .isNotEqualTo(TEST_KNOWN_DATA);
    final String result = helper.toStringFromBase64(base64);
    assertThat(result)
        .isEqualTo(TEST_KNOWN_DATA);
  }

  @Test
  void base64RoundTrip_bytes() {
    final byte[] bytes = new byte[64];
    random.nextBytes(bytes);

    final String base64 = helper.toBase64(bytes);
    final byte[] result = helper.toBytesFromBase64(base64);
    assertThat(result)
        .containsExactly(bytes)
        .isEqualTo(bytes);
  }

}