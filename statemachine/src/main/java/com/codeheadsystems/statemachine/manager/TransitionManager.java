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

import static com.codahale.metrics.MetricRegistry.name;

import com.codeheadsystems.statemachine.exceptions.TransitionException;
import com.codeheadsystems.statemachine.model.ActiveStateMachine;
import com.codeheadsystems.statemachine.model.InvocationModel;
import com.codeheadsystems.statemachine.model.State;
import com.codeheadsystems.statemachine.model.StateMachine;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the transitions of stateful objects.
 */
@Singleton
public class TransitionManager {

  private static final Logger log = LoggerFactory.getLogger(TransitionManager.class);

  private final InvocationManager invocationManager;
  private final MetricManager metricManager;
  private final LockManager lockManager;

  @Inject
  public TransitionManager(final InvocationManager invocationManager,
                           final MetricManager metricManager,
                           final LockManager lockManager) {
    this.lockManager = lockManager;
    log.debug("TransitionManager({},{})", invocationManager, metricManager);
    this.metricManager = metricManager;
    this.invocationManager = invocationManager;
  }

  /**
   * Transitions the state of the object. This method will handle the pre and post states for the transition
   * as they come up.
   *
   * @param stateMachine   that owns the transition.
   * @param transitionName that we are going to call
   * @param model          that descriptions how to execute the request.
   * @param object         that has the state.
   * @param <T>            type of object so we can return it back.
   * @return the object post transition.
   */
  public <T> T transition(final StateMachine stateMachine,
                          final InvocationModel<T> model,
                          final T object,
                          final String transitionName) {
    log.debug("transition({},{},{})", stateMachine.identifier(), transitionName, object);
    return metricManager.time(name(TransitionManager.class, "transition", stateMachine.name(), transitionName), () -> {
      model.pendingTransitionHooks().forEach(h -> h.transition(object, transitionName));
      lockManager.transitionUnderLock(stateMachine, object, () -> stateChange(stateMachine, model, object, transitionName));
      model.postTransitionHooks().forEach(h -> h.transition(object, transitionName));
      return object;
    });
  }

  /**
   * This method isolates the state change request. This allows for various locking techniques.
   *
   * @param stateMachine   that owns the transition.
   * @param transitionName that we are going to call
   * @param model          that descriptions how to execute the request.
   * @param object         that has the state.
   * @param <T>            type of object so we can return it back.
   */
  private <T> void stateChange(final StateMachine stateMachine, final InvocationModel<T> model, final T object, final String transitionName) {
    metricManager.time(name(TransitionManager.class, "stateChange", stateMachine.name(), transitionName), () -> {
      final String currentState = invocationManager.get(model, object);
      final String nextStateName = stateMachine.nextState(currentState, transitionName)
          .orElseThrow(() -> new TransitionException(stateMachine, String.format("Invalid Transition Request :%s:%s", currentState, transitionName)));
      invocationManager.set(model, object, nextStateName);
      log.info("[{}] {}({})/{} {}->{}", stateMachine.identifier(), object.getClass().getSimpleName(), object, transitionName, currentState, nextStateName);
    });
  }

  /**
   * Transitions the state of the object. This method will handle the pre and post states for the transition
   * as they come up.
   *
   * @param activeStateMachine that owns the transition and model.
   * @param transitionName     that we are going to call
   * @param object             that has the state.
   * @param <T>                type of object so we can return it back.
   * @return the object post transition.
   */
  public <T> T transition(final ActiveStateMachine<T> activeStateMachine,
                          final T object,
                          final String transitionName) {
    return transition(activeStateMachine.stateMachine(), activeStateMachine.invocationModel(), object, transitionName);
  }

  /**
   * Returns a set of transitions available for the target object in its current state.
   *
   * @param activeStateMachine activeStateMachine for the target.
   * @param targetObject       object with the state.
   * @param <T>                type of object.
   * @return set of possible transitions.
   */
  public <T> Set<String> transitions(final ActiveStateMachine<T> activeStateMachine,
                                     final T targetObject) {
    log.debug("transitions({},{})", activeStateMachine, targetObject);
    final StateMachine stateMachine = activeStateMachine.stateMachine();
    return metricManager.time(name(TransitionManager.class, "transitions", stateMachine.name()), () -> {
      final String stateName = invocationManager.get(activeStateMachine.invocationModel(), targetObject);
      if (stateName == null) {
        log.warn("Transition without state: {}", targetObject);
        throw new TransitionException(stateMachine, "No state for" + targetObject);
      }
      final State state = activeStateMachine.stateMachine().states().get(stateName);
      if (state == null) {
        log.warn("Missing state {} in state machine {} for target {}", stateName, stateMachine.identifier(), targetObject.getClass());
        throw new TransitionException(stateMachine, "No state in state machine: " + stateName);
      }
      return state.transitions().keySet();
    });
  }

}
