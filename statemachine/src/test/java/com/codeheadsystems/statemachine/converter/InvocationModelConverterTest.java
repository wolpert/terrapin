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

package com.codeheadsystems.statemachine.converter;

import static com.codeheadsystems.statemachine.Hook.PendingTransition;
import static com.codeheadsystems.statemachine.Hook.PostTransition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.statemachine.annotation.StateTarget;
import com.codeheadsystems.statemachine.model.InvocationModel;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvocationModelConverterTest {

  @Mock private PendingTransition pendingTransition;
  @Mock private PostTransition postTransition;

  private InvocationModelConverter converter;
  private Set<PendingTransition> pendingTransitionSet;
  private Set<PostTransition> postTransitionSet;

  @BeforeEach
  void setUp() {
    pendingTransitionSet = ImmutableSet.of(pendingTransition);
    postTransitionSet = ImmutableSet.of(postTransition);
    converter = new InvocationModelConverter(pendingTransitionSet, postTransitionSet);
  }

  @Test
  void generate() {
  }

  @Test
  void testGenerate() {
  }

  @Test
  void generate_fieldAnnotation() throws NoSuchMethodException {
    final InvocationModel<FieldTestSample> result = converter.generate(FieldTestSample.class);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("pendingTransitionHooks", pendingTransitionSet)
        .hasFieldOrPropertyWithValue("postTransitionHooks", postTransitionSet)
        .hasFieldOrPropertyWithValue("targetClass", FieldTestSample.class)
        .hasFieldOrPropertyWithValue("propertyName", "weirdName")
        .hasFieldOrPropertyWithValue("retrieveMethod", FieldTestSample.class.getMethod("getWeirdName"))
        .hasFieldOrPropertyWithValue("updateMethod", FieldTestSample.class.getMethod("setWeirdName", String.class));
  }

  @Test
  void generate_implicit() throws NoSuchMethodException {
    final InvocationModel<BeanSample> result = converter.generate(BeanSample.class);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("pendingTransitionHooks", pendingTransitionSet)
        .hasFieldOrPropertyWithValue("postTransitionHooks", postTransitionSet)
        .hasFieldOrPropertyWithValue("targetClass", BeanSample.class)
        .hasFieldOrPropertyWithValue("propertyName", "state")
        .hasFieldOrPropertyWithValue("retrieveMethod", BeanSample.class.getMethod("getState"))
        .hasFieldOrPropertyWithValue("updateMethod", BeanSample.class.getMethod("setState", String.class));
  }

  @Test
  void generate_bean() throws NoSuchMethodException {
    final InvocationModel<BeanSample> result = converter.generate(
        BeanSample.class, "state");

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("pendingTransitionHooks", pendingTransitionSet)
        .hasFieldOrPropertyWithValue("postTransitionHooks", postTransitionSet)
        .hasFieldOrPropertyWithValue("targetClass", BeanSample.class)
        .hasFieldOrPropertyWithValue("propertyName", "state")
        .hasFieldOrPropertyWithValue("retrieveMethod", BeanSample.class.getMethod("getState"))
        .hasFieldOrPropertyWithValue("updateMethod", BeanSample.class.getMethod("setState", String.class));
  }

  @Test
  void generate_explicit() throws NoSuchMethodException {
    final InvocationModel<BeanSample> result = converter.generate(
        BeanSample.class, "state", "getState", "setState");

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("pendingTransitionHooks", pendingTransitionSet)
        .hasFieldOrPropertyWithValue("postTransitionHooks", postTransitionSet)
        .hasFieldOrPropertyWithValue("targetClass", BeanSample.class)
        .hasFieldOrPropertyWithValue("propertyName", "state")
        .hasFieldOrPropertyWithValue("retrieveMethod", BeanSample.class.getMethod("getState"))
        .hasFieldOrPropertyWithValue("updateMethod", BeanSample.class.getMethod("setState", String.class));
  }

  @Test
  void generate_explicit_badMethod() throws NoSuchMethodException {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> converter.generate(BeanSample.class, "state", "doesnotexist", "setState"));
  }

  @Test
  void generate_explicit_badAccess() throws NoSuchMethodException {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> converter.generate(BeanSample.class, "state", "internalGetState", "setState"));
  }

  static class BeanSample {

    private String state;

    public BeanSample(final String state) {
      setState(state);
    }

    public String getState() {
      return state;
    }

    public void setState(final String state) {
      this.state = state;
    }

    private String internalGetState() {
      return state;
    }


  }

  static class FieldTestSample {

    @StateTarget
    private String weirdName;

    public FieldTestSample(final String weirdName) {
      setWeirdName(weirdName);
    }

    public String getWeirdName() {
      return weirdName;
    }

    public void setWeirdName(final String weirdName) {
      this.weirdName = weirdName;
    }

  }
}