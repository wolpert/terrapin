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

package com.codeheadsystems.statemachine.functional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.statemachine.annotation.StateMachineTarget;
import com.codeheadsystems.statemachine.exceptions.StateMachineException;
import com.codeheadsystems.statemachine.factory.ObjectMapperFactory;
import com.codeheadsystems.statemachine.factory.StateMachineFactory;
import com.codeheadsystems.statemachine.manager.StateMachineManager;
import com.codeheadsystems.statemachine.model.StateMachine;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StateMachineManagerFunctionalTest {

  public static final String SIMPLE_STATE_MACHINE = "simpleStateMachine.json";
  public static final String BAD_SIMPLE_STATE_MACHINE = "badSimpleStateMachine.json";

  public static final String ID = "ID";
  private StateMachineManager manager;

  private static Stream<Arguments> resourceExamples() {
    return Stream.of(
        Arguments.of(SIMPLE_STATE_MACHINE, Charset.defaultCharset(), StateMachineManagerFunctionalTest.class.getClassLoader(), null),
        Arguments.of(BAD_SIMPLE_STATE_MACHINE, Charset.defaultCharset(), StateMachineManagerFunctionalTest.class.getClassLoader(), StateMachineException.class)
    );
  }

  private static Stream<Arguments> inputStreamExamples() {
    return Stream.of(
        Arguments.of(inputStream(SIMPLE_STATE_MACHINE), null),
        Arguments.of(inputStream(BAD_SIMPLE_STATE_MACHINE), StateMachineException.class)
    );
  }

  private static InputStream inputStream(final String name) {
    try {
      return IOUtils.resourceToURL(name, StateMachineManagerFunctionalTest.class.getClassLoader()).openStream();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static Stream<Arguments> jsonExamples() {
    return Stream.of(
        Arguments.of("{\"name\":\"name\",\"version\":1, \"id\":\"" + ID + "\"}", null),
        Arguments.of("{\"name\":\"name\",\"id\":\"" + ID + "\",\"version\":1,\"states\":{\"s1\":{\"name\":\"s1\"}},\"s2\":{\"name\":\"s2\",\"transitions\":{}}}}\n", null),
        Arguments.of("{\"name\":\"name\",\"id\":\"" + ID + "\",\"version\":1,\"states\":{\"s1\":{\"name\":\"s1\",\"transitions\":{}}},\"s2\":{\"name\":\"s2\",\"transitions\":{}}}}", null),
        Arguments.of("{\"name\":\"name\",\"id\":\"" + ID + "\",\"version\":1,\"states\":{\"s1\":{\"name\":\"s1\",\"transitions\":{\"t\":{\"name\":\"t\",\"nextState\":\"s2\"}}},\"s2\":{\"name\":\"s2\",\"transitions\":{}}}}", null),
        Arguments.of("{\"name\":\"name\",\"id\":\"" + ID + "\",\"version\":1,\"states\":{\"s1\":{\"name\":\"s1\",\"transitions\":{\"t\":{\"name\":\"t\",\"nextState\":\"s3\"}}},\"s2\":{\"name\":\"s2\",\"transitions\":{}}}}", StateMachineException.class),
        Arguments.of("{\"name\":\"name\",\"version\":1", IllegalArgumentException.class)
    );
  }

  @BeforeEach
  void setUp() {
    final ObjectMapperFactory objectMapperFactory = new ObjectMapperFactory();
    final ObjectMapper objectMapper = objectMapperFactory.objectMapper();
    final StateMachineFactory stateMachineFactory = new StateMachineFactory();
    this.manager = new StateMachineManager(stateMachineFactory, objectMapper);
  }

  @ParameterizedTest
  @MethodSource("jsonExamples")
  void generate(final String json, final Class<? extends Throwable> throwable) {
    if (throwable == null) {
      final StateMachine result = manager.generate(json);
      assertThat(result)
          .isNotNull()
          .extracting("id")
          .isNotEqualTo(ID);
    } else {
      assertThatExceptionOfType(throwable)
          .isThrownBy(() -> manager.generate(json));
    }
  }

  @ParameterizedTest
  @MethodSource("inputStreamExamples")
  void generate(final InputStream inputStream, final Class<? extends Throwable> throwable) {
    if (throwable == null) {
      final StateMachine result = manager.generate(inputStream);
      assertThat(result)
          .isNotNull();
    } else {
      assertThatExceptionOfType(throwable)
          .isThrownBy(() -> manager.generate(inputStream));
    }
  }

  @ParameterizedTest
  @MethodSource("resourceExamples")
  void generate_resourceExamples(final String name, final Charset charset, final ClassLoader classLoader, final Class<? extends Throwable> throwable) {
    if (throwable == null) {
      final StateMachine result = manager.generate(name, charset, classLoader);
      assertThat(result)
          .isNotNull();
    } else {
      assertThatExceptionOfType(throwable)
          .isThrownBy(() -> manager.generate(name, charset, classLoader));
    }
  }

  @Test
  void generateWithAnnotation_success() {
    final Optional<StateMachine> result = manager.generateFromAnnotation(SimpleTarget.class);

    assertThat(result)
        .isPresent();
  }

  @Test
  void generateWithAnnotation_noAnnotation() {
    final Optional<StateMachine> result = manager.generateFromAnnotation(Object.class);

    assertThat(result)
        .isNotPresent();
  }

  @Test
  void generateWithAnnotation_badStateMachine() {
    assertThatExceptionOfType(StateMachineException.class)
        .isThrownBy(() -> manager.generateFromAnnotation(BadTarget.class));
  }

  @StateMachineTarget(SIMPLE_STATE_MACHINE)
  public static class SimpleTarget {

    private String state;

    public String getState() {
      return state;
    }

    public void setState(final String state) {
      this.state = state;
    }
  }

  @StateMachineTarget(BAD_SIMPLE_STATE_MACHINE)
  public static class BadTarget {

    private String state;

    public String getState() {
      return state;
    }

    public void setState(final String state) {
      this.state = state;
    }
  }
}