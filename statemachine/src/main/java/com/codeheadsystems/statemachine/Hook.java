package com.codeheadsystems.statemachine;

public interface Hook {

  @FunctionalInterface
  interface PendingTransition {
    <T> void transition(T object, String transitionName);
  }

  @FunctionalInterface
  interface PostTransition {
    <T> void transition(T object, String transitionName);
  }
}
