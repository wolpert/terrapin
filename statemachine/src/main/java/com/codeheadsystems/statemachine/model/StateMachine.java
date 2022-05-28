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

package com.codeheadsystems.statemachine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * Purpose: A collection of states with their transitions. Contains pre/post state executions as well.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableStateMachine.class)
@JsonDeserialize(builder = ImmutableStateMachine.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StateMachine {

  /**
   * Represents the name in a meaningful way to the calling application.
   *
   * @return name.
   */
  @JsonProperty("name")
  public abstract String name();

  /**
   * The identifier for this version. New instances will always have new identifiers. (Specific to the VM)
   *
   * @return id.
   */
  @JsonProperty("id")
  public abstract String id();

  /**
   * Version of the state machine. Adding states or transitions will up the version.
   *
   * @return version.
   */
  @JsonProperty("version")
  public abstract Long version();

  @JsonProperty("states")
  public abstract Map<String, State> states();

  @JsonProperty("initialState")
  public abstract Optional<String> initialState();

  @JsonIgnore
  public boolean hasState(final State state) {
    return hasState(state.name());
  }

  @JsonIgnore
  public boolean hasState(final String name) {
    return states().containsKey(name);
  }

  @JsonIgnore
  public Optional<String> nextState(final String stateName, final String transitionName) {
    return Optional.of(stateName)
        .map(s -> states().get(s))
        .map(s -> s.transitions().get(transitionName))
        .map(t -> states().get(t.nextState()))
        .map(State::name);
  }

  @JsonIgnore
  @Value.Derived
  public String identifier() {
    return String.format("%s:%s:%s", name(), version(), id());
  }

}
