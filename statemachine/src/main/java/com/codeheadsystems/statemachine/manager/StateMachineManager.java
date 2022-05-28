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

import com.codeheadsystems.statemachine.annotation.StateMachineTarget;
import com.codeheadsystems.statemachine.exceptions.StateMachineException;
import com.codeheadsystems.statemachine.factory.StateMachineFactory;
import com.codeheadsystems.statemachine.model.ImmutableStateMachine;
import com.codeheadsystems.statemachine.model.StateMachine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides manager for creation of state machines, including loading from files..
 */
@Singleton
public class StateMachineManager {

  private static final Logger log = LoggerFactory.getLogger(StateMachineManager.class);
  private final StateMachineFactory factory;
  private final ObjectMapper objectMapper;

  @Inject
  public StateMachineManager(final StateMachineFactory factory,
                             final ObjectMapper objectMapper) {
    log.debug("StateMachineManager({},{})", factory, objectMapper);
    this.objectMapper = objectMapper;
    this.factory = factory;
    if (!objectMapper.getRegisteredModuleIds().contains(Jdk8Module.class.getCanonicalName())) {
      log.warn("Missing Jdk8Module module from object mapper. Likely will have json failures for state machines.");
    }
  }

  public <T> Optional<StateMachine> generateFromAnnotation(final Class<T> targetClass) {
    final StateMachineTarget target = targetClass.getAnnotation(StateMachineTarget.class);
    if (target == null) {
      return Optional.empty();
    } else {
      return Optional.of(generate(target.value(), Charset.defaultCharset(), targetClass.getClassLoader()));
    }
  }

  /**
   * This will load from the resource if able to.
   *
   * @param resource    to use.
   * @param charset     for decoding.
   * @param classLoader classloader, since you likely don't want us.
   * @return a state machine if found.
   */
  public StateMachine generate(final String resource, final Charset charset, final ClassLoader classLoader) {
    log.debug("generate({},{},{})", resource, charset, classLoader);
    try {
      return generate(IOUtils.resourceToString(resource, charset, classLoader));
    } catch (IOException e) {
      throw new IllegalArgumentException("Unknown resource: " + resource, e);
    }
  }

  /**
   * Safe mechanism to handle.
   * TODO: Update this to limit quantity of data to jackson... DOS possible.
   *
   * @param inputStream to read from.
   * @return state machine.
   */
  public StateMachine generate(final InputStream inputStream) {
    log.debug("generate(inputStream)");
    return validateAndReId(getStateMachine(inputStream));
  }

  public StateMachine generate(final String json) {
    log.debug("generate({})", json);
    return validateAndReId(getStateMachine(json));
  }

  private StateMachine validateAndReId(StateMachine stateMachine) {
    if (!factory.isValid(stateMachine)) {
      throw new StateMachineException(stateMachine, "State Machine is not valid:" + stateMachine.toString());
    }
    return ImmutableStateMachine.copyOf(stateMachine)
        .withId(UUID.randomUUID().toString());
  }

  private StateMachine getStateMachine(final String json) {
    try {
      return objectMapper.readValue(json, StateMachine.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Bad json for stateMachine", e);
    }
  }

  private StateMachine getStateMachine(final InputStream inputStream) {
    try {
      return objectMapper.readValue(inputStream, StateMachine.class);
    } catch (IOException e) {
      throw new IllegalArgumentException("Bad json for stateMachine", e);
    }
  }

}
