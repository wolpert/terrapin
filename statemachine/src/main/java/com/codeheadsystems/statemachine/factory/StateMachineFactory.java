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

import static com.codeheadsystems.statemachine.util.Functional.add;
import static com.codeheadsystems.statemachine.util.Functional.is;

import com.codeheadsystems.statemachine.model.ImmutableState;
import com.codeheadsystems.statemachine.model.ImmutableStateMachine;
import com.codeheadsystems.statemachine.model.ImmutableTransition;
import com.codeheadsystems.statemachine.model.State;
import com.codeheadsystems.statemachine.model.StateMachine;
import com.codeheadsystems.statemachine.model.Transition;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to create instances of the state machine model, and validation.
 * <p>
 * Naming nit: This is more of a builder but not from the traditional sense of a builder pattern.
 * Calling it a factory until someone comes up with a better name.
 */
@Singleton
public class StateMachineFactory {

  protected static final Long VERSION = 1L;
  private static final Logger log = LoggerFactory.getLogger(StateMachineFactory.class);

  @Inject
  public StateMachineFactory() {
    log.debug("StateMachineFactory()");
  }

  /**
   * Creates a state machine with the expected defaults.
   *
   * @param name of the state machine.
   * @return constructed state machine.
   */
  public StateMachine generateStateMachine(final String name) {
    log.debug("generateStateMachine({})", name);
    return ImmutableStateMachine.builder()
        .id(UUID.randomUUID().toString())
        .name(name)
        .version(VERSION)
        .build();
  }

  public StateMachine addInitialState(final StateMachine stateMachine, final String initialState) {
    log.debug("addInitialState({},{})", stateMachine.identifier(), initialState);
    if (is(stateMachine.initialState(), initialState)) {
      return addState(stateMachine, initialState); // just in case it doesn't exist.
    }
    return ImmutableStateMachine.copyOf(addState(stateMachine, initialState))
        .withInitialState(initialState)
        .withVersion(stateMachine.version() + 1);
  }

  public StateMachine addTransition(final StateMachine stateMachine,
                                    final String fromState,
                                    final String transition,
                                    final String toState) {
    log.debug("addTransition({},{},{},{})", stateMachine, fromState, transition, toState);
    final StateMachine withFromState = addState(stateMachine, fromState); // verify state is in the machine.
    final StateMachine withFromAndToState = addState(withFromState, toState);
    final State state = withFromAndToState.states().get(fromState);
    if (state.hasTransition(transition)) {
      log.warn("Transition exists in state.");
      return withFromAndToState;
    } else {
      final Transition newTransition = ImmutableTransition.builder()
          .name(transition).nextState(toState).build();
      final State newFromState = ImmutableState.copyOf(state)
          .withTransitions(add(state.transitions(), transition, newTransition));
      return ImmutableStateMachine.copyOf(replaceOrAdd(withFromAndToState, newFromState))
          .withVersion(stateMachine.version() + 1L);
    }
  }

  public StateMachine addTransition(final StateMachine stateMachine,
                                    final State fromState,
                                    final Transition transition) {
    log.debug("addTransition({},{},{})", stateMachine, fromState, transition);
    final StateMachine withFromState = addState(stateMachine, fromState); // verify state is in the machine.
    final StateMachine withFromAndToState = addState(withFromState, transition.nextState());
    if (fromState.hasTransition(transition.name())) {
      log.warn("Transition exists in state.");
      return withFromAndToState;
    } else {
      final State newFromState = ImmutableState.copyOf(fromState)
          .withTransitions(add(fromState.transitions(), transition.name(), transition));
      return ImmutableStateMachine.copyOf(replaceOrAdd(withFromAndToState, newFromState))
          .withVersion(stateMachine.version() + 1L);
    }
  }

  public StateMachine addState(final StateMachine stateMachine,
                               final State state) {
    log.debug("addState({},{})", stateMachine, state);
    if (stateMachine.hasState(state)) {
      log.warn("State exists in state machine.");
      return stateMachine;
    } else {
      return replaceOrAdd(stateMachine, state);
    }
  }

  public StateMachine addState(final StateMachine stateMachine,
                               final String name) {
    log.debug("addState({},{})", stateMachine, name);
    if (stateMachine.hasState(name)) {
      return stateMachine;
    } else {
      return replaceOrAdd(stateMachine, ImmutableState.builder()
          .name(name)
          .build());
    }
  }

  public boolean isValid(final StateMachine stateMachine) {
    // Currently, only checks that the state machine's transitions all have states in the state machine.
    final boolean allStatesDefined = stateMachine.states().values().stream()
        .map(State::transitions)
        .flatMap(t -> t.values().stream())
        .map(Transition::nextState)
        .allMatch(stateMachine::hasState);
    final boolean statesNameMatches = stateMachine.states().entrySet().stream()
        .allMatch(es -> es.getKey().equals(es.getValue().name()));
    final boolean validInitialState = (!stateMachine.initialState().isPresent() || stateMachine.hasState(stateMachine.initialState().get()));
    log.info("isValid({}) -> allStatesDefined:{}, statesNameMatches={}, validInitialState={}",
        stateMachine.identifier(), allStatesDefined, statesNameMatches, validInitialState);
    return allStatesDefined && statesNameMatches && validInitialState;
  }

  private ImmutableStateMachine replaceOrAdd(final StateMachine stateMachine, final State state) {
    return ImmutableStateMachine.copyOf(stateMachine)
        .withStates(add(stateMachine.states(), state.name(), state))
        .withVersion(stateMachine.version() + 1L);
  }
}
