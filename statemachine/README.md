# State Machine

## Purpose
Provide a general purpose state machine with transition support.

## Usage
```java
  final Context context = Context.builder().build();
  context.register(statefulObject.class);
  
  context.transition(statefullObject, "next state");
  final Set<String> transitionNames = context.transitions(statefulObject);
  StatefulObject newObject = new StatefulObject();
  context.initialState(newObject);
  context.nextState(newObject);
```

## Features
* Can define JSON-friendly state machines. (Can load from external sources.)
* Register state machines for specific classes.
* Auto-discovery of variable name for state, or annotation support.
* Can specify state machine via annotation on registered class.
* Provides 'hooks' into pre/post transition methods.
* Metrics available by default.
* Strong version of state machines.
* Thread safe*. (See below for issues and what makes thread safety fail in this project.)

## Developer Notes

### Managers/Factories
* StateMachineFactory: Used to create and validate state machines
* StateMachineManager: Loads State Machines.
* TransitionManager: Manages an actual transition.
* InvocationManager: Calls methods on objects that contains states.
* Context: Handles a running set of state machines. Typically, you only need one per application.

### Dependencies
* Metrics from DropWizard
* Jackson for JSON usages
* Immutables/Guava for models
* SLF4J/Logback

### In process
* Metrics should be optional, and more granular.
  * User-provided metrics tooling as well.
* Lock support at the transition level
* Pre/Post transition support
* Registration hooks

### Thread Safety
Much was done to make this project thread safe. However, due to not having control on the stateful objects given
to the context, very little can be made about guarantees. However, you have the ability to add in a locking 
plugin model to the context that can be used to help increase thread safety.

#### Context
The building and manipulation of the context is not thread safe. However, the context should be built before actively

#### Hooks
Hooks and locking mechanism are being reworked to increase thread safety. Note your hooks can always make things worse... so be mindful
with your hooks. The pluggable locking model is in rework as well.

#### Stateful objects
State changes on objects are typically not thread-safe. Depending on the use of the pluggable locking mechanism in-play,
you can make them thread safe. But be aware, der be dragons here...