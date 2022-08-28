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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(UniqueStringExtension.class)
public class UniqueStringExtensionTest {

  public static final String FAIL = "fail";
  public static final String PREFIX = "PREFIX";
  public static final String SEPARATOR = ":";
  @UniqueString
  private String defaultString = FAIL;

  @UniqueString(prefix = PREFIX)
  private String prefixSet = FAIL;

  @UniqueString(separator = SEPARATOR)
  private String separatorSet = FAIL;

  @UniqueString(prefix = PREFIX, separator = SEPARATOR)
  private String bothSet = FAIL;

  @Test
  public void testDefault() {
    assertThat(defaultString)
        .isNotNull()
        .isNotEqualTo(FAIL)
        .contains("unique")
        .contains("_");
  }

  @Test
  public void testPrefix() {
    assertThat(prefixSet)
        .isNotNull()
        .isNotEqualTo(FAIL)
        .contains(PREFIX)
        .contains("_");

  }

  @Test
  public void testSeparator() {
    assertThat(separatorSet)
        .isNotNull()
        .isNotEqualTo(FAIL)
        .contains("unique")
        .contains(SEPARATOR);

  }

  @Test
  public void testBoth() {
    assertThat(bothSet)
        .isNotNull()
        .isNotEqualTo(FAIL)
        .contains(PREFIX)
        .contains(SEPARATOR);

  }

}
