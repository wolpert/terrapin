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

package com.codeheadsystems.statemachine.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.statemachine.BaseMetricTest;
import com.codeheadsystems.statemachine.exceptions.TargetException;
import com.codeheadsystems.statemachine.model.ImmutableInvocationModel;
import com.codeheadsystems.statemachine.model.InvocationModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvocationManagerTest extends BaseMetricTest {

  private Method getState;
  private Method setState;
  private Method internalGetState;
  private Method badSetState;

  private InvocationManager manager;

  @BeforeEach
  void setUp() throws NoSuchMethodException {
    getState = SampleClass.class.getDeclaredMethod("getState");
    setState = SampleClass.class.getDeclaredMethod("setState", String.class);
    internalGetState = SampleClass.class.getDeclaredMethod("internalGetState");
    badSetState = SampleClass.class.getDeclaredMethod("badSetState", String.class);
    manager = new InvocationManager(metricManager);
  }

  @Test
  void set() {
    final SampleClass sample = new SampleClass("this");
    final InvocationModel<SampleClass> model = ImmutableInvocationModel.<SampleClass>builder()
        .updateMethod(setState).retrieveMethod(getState)
        .propertyName("state").targetClass(SampleClass.class)
        .build();

    manager.set(model, sample, "that");

    assertThat(sample)
        .hasFieldOrPropertyWithValue(model.propertyName(), "that");
  }

  @Test
  void get() {
    final SampleClass sample = new SampleClass("this");
    final InvocationModel<SampleClass> model = ImmutableInvocationModel.<SampleClass>builder()
        .updateMethod(setState).retrieveMethod(getState)
        .propertyName("state").targetClass(SampleClass.class)
        .build();

    final String result = manager.get(model, sample);

    assertThat(result)
        .isNotNull()
        .isNotEmpty()
        .isEqualTo("this");
  }

  @Test
  void get_null() {
    final SampleClass sample = new SampleClass(null);
    final InvocationModel<SampleClass> model = ImmutableInvocationModel.<SampleClass>builder()
        .updateMethod(setState).retrieveMethod(getState)
        .propertyName("state").targetClass(SampleClass.class)
        .build();

    final String result = manager.get(model, sample);

    assertThat(result)
        .isNull();
  }

  @Test
  void transition_invocationException() {
    final SampleClass sample = new SampleClass("this");
    final InvocationModel<SampleClass> model = ImmutableInvocationModel.<SampleClass>builder()
        .updateMethod(badSetState).retrieveMethod(getState)
        .propertyName("state").targetClass(SampleClass.class)
        .build();


    assertThatExceptionOfType(TargetException.class)
        .isThrownBy(() -> manager.set(model, sample, "that"))
        .withCauseInstanceOf(InvocationTargetException.class);
  }

  @Test
  void transition_illegalAccess() {
    final SampleClass sample = new SampleClass("this");
    final InvocationModel<SampleClass> model = ImmutableInvocationModel.<SampleClass>builder()
        .updateMethod(setState).retrieveMethod(internalGetState)
        .propertyName("state").targetClass(SampleClass.class)
        .build();

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> manager.get(model, sample))
        .withCauseInstanceOf(IllegalAccessException.class);
  }

  @Test
  void transition_illegalArgument() {
    final SampleClass sample = new SampleClass("this");
    final InvocationModel<SampleClass> model = ImmutableInvocationModel.<SampleClass>builder()
        .updateMethod(setState).retrieveMethod(setState)
        .propertyName("state").targetClass(SampleClass.class)
        .build();

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> manager.get(model, sample))
        .withCauseInstanceOf(IllegalArgumentException.class);
  }

  static class SampleClass {

    private String state;

    public SampleClass(final String state) {
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

    public void badSetState(final String state) {
      throw new RuntimeException("blah");
    }

  }
}