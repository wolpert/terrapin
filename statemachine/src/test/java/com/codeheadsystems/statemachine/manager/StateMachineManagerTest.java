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
import static org.mockito.Mockito.when;

import com.codeheadsystems.statemachine.exceptions.StateMachineException;
import com.codeheadsystems.statemachine.factory.StateMachineFactory;
import com.codeheadsystems.statemachine.model.ImmutableState;
import com.codeheadsystems.statemachine.model.ImmutableStateMachine;
import com.codeheadsystems.statemachine.model.ImmutableTransition;
import com.codeheadsystems.statemachine.model.StateMachine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StateMachineManagerTest {

  public static final String JSON = "json";
  public static final String FIRST_STATE = "s1";
  public static final String SECOND_STATE = "s2";
  public static final String TRANSITION = "t";
  private static final StateMachine MACHINE = ImmutableStateMachine.builder()
      .name("name")
      .version(1L)
      .id("id")
      .putStates(FIRST_STATE, ImmutableState.builder()
          .name(FIRST_STATE)
          .putTransitions(TRANSITION, ImmutableTransition.builder()
              .name(TRANSITION)
              .nextState(SECOND_STATE)
              .build())
          .build())
      .putStates(SECOND_STATE, ImmutableState.builder().name(SECOND_STATE).build())
      .build();

  @Mock
  private StateMachineFactory stateMachineFactory;
  @Mock
  private ObjectMapper mapper;
  @Mock
  private StateMachine machine;
  @Mock
  private InputStream inputStream;

  private StateMachineManager manager;

  @BeforeEach
  void setUp() {
    when(mapper.getRegisteredModuleIds()).thenReturn(ImmutableSet.of(Jdk8Module.class.getCanonicalName()));
    this.manager = new StateMachineManager(stateMachineFactory, mapper);
  }

  @Test
  void generate_inputStream() throws IOException {
    when(mapper.readValue(inputStream, StateMachine.class)).thenReturn(MACHINE);
    when(stateMachineFactory.isValid(MACHINE)).thenReturn(true);

    final StateMachine result = manager.generate(inputStream);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("name", MACHINE.name())
        .hasFieldOrPropertyWithValue("version", MACHINE.version())
        .hasFieldOrPropertyWithValue("states", MACHINE.states())
        .extracting("id")
        .isNotEqualTo(MACHINE.id());
  }

  @Test
  void generate_inputStream_notValid() throws IOException {
    when(mapper.readValue(inputStream, StateMachine.class)).thenReturn(machine);
    when(stateMachineFactory.isValid(machine)).thenReturn(false);

    assertThatExceptionOfType(StateMachineException.class)
        .isThrownBy(() -> manager.generate(inputStream));
  }

  @Test
  void generate_targetClass_noAnnotation() {
    final Optional<StateMachine> stateMachineOptional = manager.generateFromAnnotation(Object.class);

    assertThat(stateMachineOptional)
        .isNotPresent();
  }

  @Test
  void generate() throws JsonProcessingException {
    when(mapper.readValue(JSON, StateMachine.class)).thenReturn(MACHINE);
    when(stateMachineFactory.isValid(MACHINE)).thenReturn(true);

    final StateMachine result = manager.generate(JSON);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("name", MACHINE.name())
        .hasFieldOrPropertyWithValue("version", MACHINE.version())
        .hasFieldOrPropertyWithValue("states", MACHINE.states())
        .extracting("id")
        .isNotEqualTo(MACHINE.id());
  }

  @Test
  void generate_notValid() throws JsonProcessingException {
    when(mapper.readValue(JSON, StateMachine.class)).thenReturn(machine);
    when(stateMachineFactory.isValid(machine)).thenReturn(false);

    assertThatExceptionOfType(StateMachineException.class)
        .isThrownBy(() -> manager.generate(JSON));

  }

  @Test
  void generate_notJson() throws JsonProcessingException {
    when(mapper.readValue(JSON, StateMachine.class)).thenThrow(new OurJsonProcessingException());

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> manager.generate(JSON));

  }

  static class OurJsonProcessingException extends JsonProcessingException {
    public OurJsonProcessingException() {
      super("blah");
    }
  }

}