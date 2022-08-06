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

package com.codeheadsystems.statemachine.factory;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;

import com.codeheadsystems.statemachine.model.ImmutableState;
import com.codeheadsystems.statemachine.model.ImmutableStateMachine;
import com.codeheadsystems.statemachine.model.ImmutableTransition;
import com.codeheadsystems.statemachine.model.State;
import com.codeheadsystems.statemachine.model.StateMachine;
import com.codeheadsystems.statemachine.model.Transition;
import com.google.common.collect.ImmutableMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StateMachineFactoryTest {

  private static final String NAME = "name";
  private static final State STATE_FROM = ImmutableState.builder().name("from").build();
  private static final State STATE_TO = ImmutableState.builder().name("to").build();
  private static final Transition TRANSITION = ImmutableTransition.builder().name("t").nextState("to").build();

  private StateMachineFactory factory;

  private static Stream<Arguments> isValidArguments() {
    return Stream.of(
        Arguments.of(
            ImmutableStateMachine.builder().id("id").name("empty").version(1L).build(),
            true
        ),
        Arguments.of(
            ImmutableStateMachine.builder().id("id").name("oneState").version(2L)
                .putStates("s", ImmutableState.builder().name("s").build())
                .build(),
            true
        ),
        Arguments.of(
            ImmutableStateMachine.builder().id("id").name("oneStateWithInitialState").version(2L)
                .initialState("s")
                .putStates("s", ImmutableState.builder().name("s").build())
                .build(),
            true
        ),
        Arguments.of(
            ImmutableStateMachine.builder().id("id").name("oneStateWithBadInitialState").version(2L)
                .initialState("notS")
                .putStates("s", ImmutableState.builder().name("s").build())
                .build(),
            false
        ),
        Arguments.of(
            ImmutableStateMachine.builder().id("id").name("oneStateBadName").version(2L)
                .putStates("s", ImmutableState.builder().name("notS").build())
                .build(),
            false
        ),
        Arguments.of(
            ImmutableStateMachine.builder().id("id").name("oneStateWithTtoS").version(3L)
                .putStates("s", ImmutableState.builder().name("s")
                    .putTransitions("t", ImmutableTransition.builder().name("t").nextState("s").build())
                    .build())
                .build(),
            true
        ),
        Arguments.of(
            ImmutableStateMachine.builder().id("id").name("badTransition").version(4L)
                .putStates("s", ImmutableState.builder().name("s")
                    .putTransitions("t", ImmutableTransition.builder().name("t").nextState("notS").build())
                    .build())
                .build(),
            false
        )
    );
  }

  @BeforeEach
  public void setup() {
    factory = new StateMachineFactory();
  }

  @Test
  public void generateStateMachine() {
    final StateMachine result = factory.generateStateMachine(NAME);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("name", NAME)
        .hasFieldOrPropertyWithValue("version", StateMachineFactory.VERSION)
        .extracting("id")
        .isNotNull();
  }

  @ParameterizedTest
  @MethodSource("isValidArguments")
  void isValid(final StateMachine matchine, final boolean valid) {
    final boolean result = factory.isValid(matchine);

    assertThat(result)
        .isEqualTo(valid);
  }

  @Test
  void addTransition_withBothState_withTransition() {
    final State fromState = ImmutableState.copyOf(STATE_FROM)
        .withTransitions(ImmutableMap.of(TRANSITION.name(), TRANSITION));
    assertThat(fromState.hasTransition(TRANSITION)).isTrue();
    assertThat(fromState.hasTransition(TRANSITION.name())).isTrue();
    final StateMachine stateMachine = ImmutableStateMachine.copyOf(
            factory.generateStateMachine(NAME))
        .withStates(ImmutableMap.of(fromState.name(), fromState, STATE_TO.name(), STATE_TO));

    final StateMachine result = factory.addTransition(stateMachine, fromState, TRANSITION);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("version", 1L)
        .extracting(StateMachine::states, as(map(String.class, State.class)))
        .hasSize(2)
        .hasEntrySatisfying(STATE_FROM.name(), s -> assertThat(s)
            .isNotNull()
            .extracting(State::transitions, as(map(String.class, Transition.class)))
            .hasSize(1)
            .hasEntrySatisfying(TRANSITION.name(), t -> assertThat(t).isEqualTo(TRANSITION)))
        .hasEntrySatisfying(STATE_TO.name(), s -> assertThat(s).isEqualTo(STATE_TO));
  }

  @Test
  void addTransitionString_withBothState_withTransition() {
    final State fromState = ImmutableState.copyOf(STATE_FROM)
        .withTransitions(ImmutableMap.of(TRANSITION.name(), TRANSITION));
    assertThat(fromState.hasTransition(TRANSITION)).isTrue();
    assertThat(fromState.hasTransition(TRANSITION.name())).isTrue();
    final StateMachine stateMachine = ImmutableStateMachine.copyOf(
            factory.generateStateMachine(NAME))
        .withStates(ImmutableMap.of(fromState.name(), fromState, STATE_TO.name(), STATE_TO));

    final StateMachine result = factory.addTransition(stateMachine, STATE_FROM.name(), TRANSITION.name(), STATE_TO.name());

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("version", 1L)
        .extracting(StateMachine::states, as(map(String.class, State.class)))
        .hasSize(2)
        .hasEntrySatisfying(STATE_FROM.name(), s -> assertThat(s)
            .isNotNull()
            .extracting(State::transitions, as(map(String.class, Transition.class)))
            .hasSize(1)
            .hasEntrySatisfying(TRANSITION.name(), t -> assertThat(t).isEqualTo(TRANSITION)))
        .hasEntrySatisfying(STATE_TO.name(), s -> assertThat(s).isEqualTo(STATE_TO));
  }

  @Test
  void addTransition_withBothState_noTransition() {
    final StateMachine stateMachine = ImmutableStateMachine.copyOf(
            factory.generateStateMachine(NAME))
        .withStates(ImmutableMap.of(
            STATE_FROM.name(), STATE_FROM,
            STATE_TO.name(), STATE_TO));

    final StateMachine result = factory.addTransition(stateMachine, STATE_FROM, TRANSITION);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("version", 2L)
        .extracting(StateMachine::states, as(map(String.class, State.class)))
        .hasSize(2)
        .hasEntrySatisfying(STATE_FROM.name(), s -> assertThat(s)
            .isNotNull()
            .extracting(State::transitions, as(map(String.class, Transition.class)))
            .hasSize(1)
            .hasEntrySatisfying(TRANSITION.name(), t -> assertThat(t).isEqualTo(TRANSITION)))
        .hasEntrySatisfying(STATE_TO.name(), s -> assertThat(s).isEqualTo(STATE_TO));
  }

  @Test
  void addTransitionString_withBothState_noTransition() {
    final StateMachine stateMachine = ImmutableStateMachine.copyOf(
            factory.generateStateMachine(NAME))
        .withStates(ImmutableMap.of(
            STATE_FROM.name(), STATE_FROM,
            STATE_TO.name(), STATE_TO));

    final StateMachine result = factory.addTransition(stateMachine, STATE_FROM.name(), TRANSITION.name(), STATE_TO.name());

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("version", 2L)
        .extracting(StateMachine::states, as(map(String.class, State.class)))
        .hasSize(2)
        .hasEntrySatisfying(STATE_FROM.name(), s -> assertThat(s)
            .isNotNull()
            .extracting(State::transitions, as(map(String.class, Transition.class)))
            .hasSize(1)
            .hasEntrySatisfying(TRANSITION.name(), t -> assertThat(t).isEqualTo(TRANSITION)))
        .hasEntrySatisfying(STATE_TO.name(), s -> assertThat(s).isEqualTo(STATE_TO));
  }

  @Test
  void addInitialState_noState() {
    final StateMachine stateMachine = factory.generateStateMachine(NAME);

    final StateMachine result = factory.addInitialState(stateMachine, STATE_FROM.name());

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("version", 2L)
        .extracting(StateMachine::states, as(map(String.class, State.class)))
        .hasSize(1)
        .hasEntrySatisfying(STATE_FROM.name(), s -> assertThat(s)
            .isNotNull()
            .extracting(State::transitions, as(map(String.class, Transition.class)))
            .isEmpty()
        );
  }

  @Test
  void addInitialState_withState() {
    final StateMachine stateMachine = ImmutableStateMachine.copyOf(
            factory.generateStateMachine(NAME))
        .withStates(ImmutableMap.of(STATE_FROM.name(), STATE_FROM));

    final StateMachine result = factory.addInitialState(stateMachine, STATE_FROM.name());

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("version", 2L)
        .extracting(StateMachine::states, as(map(String.class, State.class)))
        .hasSize(1)
        .hasEntrySatisfying(STATE_FROM.name(), s -> assertThat(s)
            .isNotNull()
            .extracting(State::transitions, as(map(String.class, Transition.class)))
            .isEmpty()
        );
  }

  @Test
  void addInitialState_withState_withInitialState() {
    final StateMachine stateMachine = ImmutableStateMachine.copyOf(
            factory.generateStateMachine(NAME))
        .withInitialState(STATE_FROM.name())
        .withStates(ImmutableMap.of(STATE_FROM.name(), STATE_FROM));

    final StateMachine result = factory.addInitialState(stateMachine, STATE_FROM.name());

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("version", 1L)
        .isEqualTo(stateMachine);
  }

  @Test
  void addTransition_noState_noTransition() {
    final StateMachine stateMachine = factory.generateStateMachine(NAME);

    final StateMachine result = factory.addTransition(stateMachine, STATE_FROM, TRANSITION);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("version", 2L)
        .extracting(StateMachine::states, as(map(String.class, State.class)))
        .hasSize(2)
        .hasEntrySatisfying(STATE_FROM.name(), s -> assertThat(s)
            .isNotNull()
            .extracting(State::transitions, as(map(String.class, Transition.class)))
            .hasSize(1)
            .hasEntrySatisfying(TRANSITION.name(), t -> assertThat(t).isEqualTo(TRANSITION)))
        .hasEntrySatisfying(STATE_TO.name(), s -> assertThat(s).isEqualTo(STATE_TO));
  }

  @Test
  void addTransitionString_noState_noTransition() {
    final StateMachine stateMachine = factory.generateStateMachine(NAME);

    final StateMachine result = factory.addTransition(stateMachine, STATE_FROM.name(), TRANSITION.name(), STATE_TO.name());

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("version", 2L)
        .extracting(StateMachine::states, as(map(String.class, State.class)))
        .hasSize(2)
        .hasEntrySatisfying(STATE_FROM.name(), s -> assertThat(s)
            .isNotNull()
            .extracting(State::transitions, as(map(String.class, Transition.class)))
            .hasSize(1)
            .hasEntrySatisfying(TRANSITION.name(), t -> assertThat(t).isEqualTo(TRANSITION)))
        .hasEntrySatisfying(STATE_TO.name(), s -> assertThat(s).isEqualTo(STATE_TO));
  }

  @Test
  void addState_notExisting() {
    final StateMachine stateMachine = factory.generateStateMachine(NAME);

    final StateMachine result = factory.addState(stateMachine, STATE_FROM);

    assertThat(result)
        .isNotNull()
        .isNotEqualTo(stateMachine);
    assertThat(result.states())
        .hasSize(1)
        .hasEntrySatisfying(STATE_FROM.name(), v -> assertThat(v).isEqualTo(STATE_FROM));

  }

  @Test
  void addState_existing() {
    final StateMachine stateMachine = ImmutableStateMachine.copyOf(
            factory.generateStateMachine(NAME))
        .withStates(ImmutableMap.of(STATE_FROM.name(), STATE_FROM));

    final StateMachine result = factory.addState(stateMachine, STATE_FROM);

    assertThat(result)
        .isNotNull()
        .isEqualTo(stateMachine);
  }

  @Test
  void addState_string_notExisting() {
    final StateMachine stateMachine = factory.generateStateMachine(NAME);

    final StateMachine result = factory.addState(stateMachine, STATE_FROM.name());

    assertThat(result)
        .isNotNull()
        .isNotEqualTo(stateMachine);
    assertThat(result.states())
        .hasSize(1)
        .hasEntrySatisfying(STATE_FROM.name(),
            v -> assertThat(v).hasFieldOrPropertyWithValue("name", STATE_FROM.name()));

  }

  @Test
  void addState_string_existing() {
    final StateMachine stateMachine = ImmutableStateMachine.copyOf(
            factory.generateStateMachine(NAME))
        .withStates(ImmutableMap.of(STATE_FROM.name(), STATE_FROM));

    final StateMachine result = factory.addState(stateMachine, STATE_FROM.name());

    assertThat(result)
        .isNotNull()
        .isEqualTo(stateMachine);
  }

}