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

package com.codeheadsystems.statemachine;

import com.codeheadsystems.statemachine.converter.InvocationModelConverter;
import com.codeheadsystems.statemachine.exceptions.StateMachineException;
import com.codeheadsystems.statemachine.exceptions.TargetException;
import com.codeheadsystems.statemachine.manager.InvocationManager;
import com.codeheadsystems.statemachine.manager.MetricManager;
import com.codeheadsystems.statemachine.manager.StateMachineManager;
import com.codeheadsystems.statemachine.manager.TransitionManager;
import com.codeheadsystems.statemachine.model.ActiveStateMachine;
import com.codeheadsystems.statemachine.model.ImmutableActiveStateMachine;
import com.codeheadsystems.statemachine.model.InvocationModel;
import com.codeheadsystems.statemachine.model.StateMachine;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Purpose: Runtime context for available state machines.
 * <p>
 * How to use:
 * <p>
 * Context.builder().build();
 */
@Singleton
public class Context {

  private static final Logger log = LoggerFactory.getLogger(Context.class);
  private static final String CONTEXT_REGISTER = "Context.register";
  private static final String CONTEXT_DEREGISTER = "Context.deregister";
  private static final String CONTEXT_IS_REGISTERED = "Context.isRegistered";
  private static final String CONTEXT_TRANSITION = "Context.transition";
  private static final String CONTEXT_NEXT_STATE = "Context.nextState";
  private static final String CONTEXT_CAN_TRANSITION = "Context.canTransition";
  private static final String CONTEXT_TRANSITIONS = "Context.transitions";
  private static final String CONTEXT_GET_REGISTERED_CLASSES = "Context.getRegisteredClasses";
  private static final String CONTEXT_SET_INITIAL_STATE = "Context.setInitialState";
  private static final String CONTEXT_GET_STATE_MACHINE_FOR_CLASS = "Context.getStateMachineForClass";
  private static final String CONTEXT_GET_STATE_MACHINE = "Context.getStateMachine";
  private static final String[] METRIC_NAMES = {
      CONTEXT_REGISTER, CONTEXT_DEREGISTER, CONTEXT_IS_REGISTERED, CONTEXT_TRANSITION, CONTEXT_NEXT_STATE,
      CONTEXT_CAN_TRANSITION, CONTEXT_TRANSITIONS, CONTEXT_GET_REGISTERED_CLASSES, CONTEXT_SET_INITIAL_STATE,
      CONTEXT_GET_STATE_MACHINE_FOR_CLASS, CONTEXT_GET_STATE_MACHINE
  };

  private final String id;
  private final StateMachineManager stateMachineManager;
  private final TransitionManager transitionManager;
  private final InvocationModelConverter invocationModelConverter;
  private final InvocationManager invocationManager;
  private final MetricManager metricManager;
  private final Map<Class<?>, ActiveStateMachine<?>> stateMachineMap;

  @Inject
  Context(final StateMachineManager stateMachineManager,
          final TransitionManager transitionManager,
          final InvocationModelConverter invocationModelConverter,
          final InvocationManager invocationManager,
          final MetricManager metricManager) {
    id = UUID.randomUUID().toString();
    log.info("[{}] Context({},{},{},{},{})", id, stateMachineManager, transitionManager, invocationModelConverter, invocationManager, metricManager);
    this.stateMachineManager = stateMachineManager;
    this.transitionManager = transitionManager;
    this.invocationModelConverter = invocationModelConverter;
    this.invocationManager = invocationManager;
    this.metricManager = metricManager;
    stateMachineMap = new HashMap<>();
    Arrays.stream(METRIC_NAMES).forEach(n -> metricManager.meter(n, 0));
  }

  /**
   * Generate an instance of the state machine context. You should reuse the same context for your application,
   * and should not need to create multiple ones.
   *
   * @return context builder.
   */
  public static ContextBuilder builder() {
    return new ContextBuilder();
  }

  /**
   * Registers the target class with the state machine in the class. Requires annotations on the target
   * class for a given state machine. Still will use annotations on the class for finding the
   * correct field that holds onto the state.
   *
   * @param targetClass the state machine should apply for. (Ignores inheritance patterns)
   * @param <T>         type of object.
   */
  public <T> void register(final Class<T> targetClass) {
    final StateMachine stateMachine = stateMachineManager.generateFromAnnotation(targetClass)
        .orElseThrow(() -> new TargetException("No state machine found for class"));
    register(targetClass, stateMachine);
  }

  /**
   * Registers the target class with the given state machine. Ignores annotations on the target
   * class for a given state machine. Still will use annotations on the class for finding the
   * correct field that holds onto the state.
   *
   * @param targetClass  the state machine should apply for. (Ignores inheritance patterns)
   * @param stateMachine we are using for the class.
   * @param <T>          type of object.
   */
  public <T> void register(final Class<T> targetClass, final StateMachine stateMachine) {
    log.debug("[{}] register({},{})", id, targetClass.getCanonicalName(), stateMachine);
    metricManager.meter(CONTEXT_REGISTER, 1);
    final InvocationModel<T> model = invocationModelConverter.generate(targetClass);
    // Validate
    if (!model.targetClass().equals(targetClass)) { // Basically impossible... but have to check
      throw new IllegalStateException("Bug found! Please file a ticket. Unable to create invocation model for class: " + targetClass + ":" + model);
    }
    final ActiveStateMachine<T> activeStateMachine = ImmutableActiveStateMachine.<T>builder()
        .stateMachine(stateMachine)
        .invocationModel(model)
        .build();
    // Validate
    stateMachineMap.put(targetClass, activeStateMachine);
    log.info("[{}] Registered: {}->{}", id, targetClass.getCanonicalName(), stateMachine.identifier());
  }

  /**
   * Removes from the context the target class so we won't manage any transitions of those anymore.
   *
   * @param targetClass to deregister.
   * @param <T>         the type.
   */
  public <T> void deregister(final Class<T> targetClass) {
    log.debug("[{}] deregister({})", id, targetClass.getCanonicalName());
    metricManager.meter(CONTEXT_DEREGISTER, 1);
    final ActiveStateMachine<?> activeStateMachine = stateMachineMap.remove(targetClass);
    if (activeStateMachine != null) {
      log.info("[{}] Dergistered: {}->{}}", id, targetClass.getCanonicalName(), activeStateMachine.stateMachine().identifier());
    } else {
      log.warn("[{}] Not registered: {}", id, targetClass.getCanonicalName());
    }
  }

  /**
   * Checks to see if this class is registered.
   *
   * @param targetClass to check.
   * @param <T>         type.
   * @return true or false.
   */
  public <T> boolean isRegistered(final Class<T> targetClass) {
    log.debug("[{}] isRegistered({})", id, targetClass.getCanonicalName());
    metricManager.meter(CONTEXT_IS_REGISTERED, 1);
    return stateMachineMap.containsKey(targetClass);
  }

  /**
   * Transitions the object if the target class is registered. Throws an exception if its not.
   *
   * @param targetObject to transition.
   * @param transition   the transition.
   * @param <T>          type of object.
   */
  public <T> void transition(final T targetObject, final String transition) {
    log.debug("[{}] transition({},{})", id, targetObject, transition);
    metricManager.meter(CONTEXT_TRANSITION, 1);
    final ActiveStateMachine<T> activeStateMachine = getRequiredActiveStateMachine(targetObject);
    transitionManager.transition(activeStateMachine, targetObject, transition);
  }

  /**
   * Transitions the object to the next state if there is a next state. It will throw an exception if the
   * state machine has multiple transitions available.
   *
   * @param targetObject to transition.
   * @param <T>          type of object.
   */
  public <T> boolean nextState(final T targetObject) {
    log.debug("[{}] nextState({})", id, targetObject);
    metricManager.meter(CONTEXT_NEXT_STATE, 1);
    final ActiveStateMachine<T> activeStateMachine = getRequiredActiveStateMachine(targetObject);
    final Set<String> transitions = transitions(targetObject);
    if (transitions.isEmpty()) {
      log.debug("[{}] nextState({}) -> no next state available)", id, targetObject);
      return false;
    } else if (transitions.size() > 1) {
      log.error("[{}] nextState({}) -> too many transitions {})", id, targetObject, transitions);
      throw new StateMachineException(activeStateMachine.stateMachine(), "Too many transitions for state found: " + transitions);
    }
    transition(targetObject, transitions.toArray()[0].toString()); // should only have 1 now
    return true;
  }

  /**
   * Checks to see if this object can transition to the next state.
   *
   * @param targetObject to transition.
   * @param transition   transition we are checking for.
   * @param <T>          type of object.
   * @return boolean if we can.
   */
  public <T> boolean canTransition(final T targetObject, final String transition) {
    log.debug("[{}] canTransition({},{})", id, targetObject, transition);
    metricManager.meter(CONTEXT_CAN_TRANSITION, 1);
    return transitions(targetObject).contains(transition);
  }

  /**
   * Returns available transitions for the object if the object's class is registered.
   *
   * @param targetObject that which we want the transitions for.
   * @param <T>          type.
   * @return a set.
   */
  public <T> Set<String> transitions(final T targetObject) {
    log.debug("[{}] transitions({})", id, targetObject);
    metricManager.meter(CONTEXT_TRANSITIONS, 1);
    final ActiveStateMachine<T> activeStateMachine = getRequiredActiveStateMachine(targetObject);
    return transitionManager.transitions(activeStateMachine, targetObject);
  }

  /**
   * Returns a list of target classes we have registered.
   *
   * @return a set.
   */
  public Set<Class<?>> getRegisteredClasses() {
    log.debug("[{}] getRegisteredClasses()", id);
    metricManager.meter(CONTEXT_GET_REGISTERED_CLASSES, 1);
    return stateMachineMap.keySet();
  }

  /**
   * Sets the initial state on the target object based on the registered statemachine. It ignores the current
   * state. If the state machine does not define an initial state, this will throw an exception.
   *
   * @param target that will have its state changed.
   * @param <T>    type of object.
   */
  public <T> void setInitialState(final T target) {
    log.debug("[{}] setInitialState({})", id, target.getClass().getCanonicalName());
    metricManager.meter(CONTEXT_SET_INITIAL_STATE, 1);
    final ActiveStateMachine<T> activeStateMachine = getRequiredActiveStateMachine(target);
    final String initialState = activeStateMachine.stateMachine().initialState()
        .orElseThrow(() -> new StateMachineException(activeStateMachine.stateMachine(), "No initial state, but needed for " + target.getClass()));
    invocationManager.set(activeStateMachine.invocationModel(), target, initialState);
  }

  /**
   * Returns the state machine for the target class.
   *
   * @param targetClass which we hold the state machine for.
   * @param <T>         type of object.
   * @return an optional state machine.
   */
  public <T> Optional<StateMachine> getStateMachineForClass(final Class<T> targetClass) {
    log.debug("[{}] getStateMachineForClass({})", id, targetClass.getCanonicalName());
    metricManager.meter(CONTEXT_GET_STATE_MACHINE_FOR_CLASS, 1);
    return activeStateMachineForClass(targetClass).map(ActiveStateMachine::stateMachine);
  }

  /**
   * Returns the state machine for the target object's class.
   *
   * @param target which we hold the state machine for.
   * @param <T>    type of object.
   * @return an optional state machine.
   */
  public <T> Optional<StateMachine> getStateMachine(final T target) {
    log.debug("[{}] getStateMachine({})", id, target.getClass().getCanonicalName());
    metricManager.meter(CONTEXT_GET_STATE_MACHINE, 1);
    return activeStateMachineForClass(target.getClass()).map(ActiveStateMachine::stateMachine);
  }

  /**
   * Returns the state machine for the class, but if tot found, will throw an exception.
   *
   * @param targetObject to look for the state machine.
   * @param <T>          type of object.
   * @return a valid active state machine.
   */
  private <T> ActiveStateMachine<T> getRequiredActiveStateMachine(final T targetObject) {
    return activeStateMachine(targetObject).orElseThrow(() ->
        new IllegalArgumentException("No state machine found for class: " + targetObject.getClass()));
  }

  private <T> Optional<ActiveStateMachine<T>> activeStateMachine(final T target) {
    log.debug("[{}] activeStateMachine({})", id, target);
    final Class<T> clazz = (Class<T>) target.getClass(); // freaking java...
    return activeStateMachineForClass(clazz);
  }

  private <T> Optional<ActiveStateMachine<T>> activeStateMachineForClass(final Class<T> targetClass) {
    log.debug("[{}] activeStateMachine({})", id, targetClass.getCanonicalName());
    final ActiveStateMachine<?> activeStateMachine = stateMachineMap.get(targetClass);
    if (activeStateMachine == null) {
      return Optional.empty();
    } else {
      if (activeStateMachine.invocationModel().targetClass().equals(targetClass)) {
        final ActiveStateMachine<T> result = (ActiveStateMachine<T>) activeStateMachine;
        return Optional.of(result);
      } else {
        throw new IllegalStateException("Bug found! Please file a ticket. Invalid active machine found for " +
            targetClass.getCanonicalName() + ":" +
            activeStateMachine.stateMachine().identifier());
      }
    }
  }
}
