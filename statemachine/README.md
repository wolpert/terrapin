# State Machine

## Purpose

Provide a general purpose state machine with transition support. State machines
can be programmatically created and registered with the classes that make up the
stateful objects. Or annotations can be used to reduce to read in state machine
definitions and use them for stateful objects. Pre/Post hooks available for
transitions as well as a pluggable locking model to help resolve thread-safety
issues.

## Release

gradle

```groovy
repositories {
    jcenter()
}
dependencies {
    implementation 'com.codeheadsystems.terrapin:statemachine:1.0'
}
```

## Usage (simple)

```java
  final Context context = Context.builder().build();
  context.register(StatefulObject.class);
  
  context.transition(statefullObject, "next state");
  final Set<String> transitionNames = context.transitions(statefulObject);
  StatefulObject newObject = new StatefulObject();
  context.initialState(newObject);
  context.nextState(newObject);
```

For more examples, check out the functional tests.

## Features

* Can define JSON-friendly state machines. (Can load from external sources.)
* Register state machines for specific classes.
* Auto-discovery of variable name for state, or annotation support.
* Can specify state machine via annotation on registered class.
* Provides 'hooks' into pre/post transition methods.
* Metrics available by default.
* Strong version of state machines.
* Thread safe*. (See below for issues and what makes thread safety fail in this
  project.)

## Developer Notes

This was originally a Java version of the RubyOnRails project
activerecord-statemachine. It has grown in complexity and was reworked to use
Immutables for the internal model. Though this project tries to be thread-safe,
its highly dependent on how the stateful objects are used during the
transitions, and code that is executed for the pre/post hooks. I removed the old
plugable locking mechanism and hook code with the goal of trying to rework it so
it's easier to use and better performance. Also, I reworked the structure of the
code to follow the 'manager-model' layout.

### Managers/Factories

* StateMachineFactory: Used to create and validate state machines
* StateMachineManager: Loads State Machines.
* TransitionManager: Manages an actual transition.
* InvocationManager: Calls methods on objects that contains states.
* Context: Handles a running set of state machines. Typically, you only need one
  per application.

### Dependencies

* Metrics from DropWizard
* Jackson for JSON usages
* Immutables/Guava for models.
* Dagger for IOC
* SLF4J/Logback

### In process

* Allowing for AtomicReference to hold the state of the target object. (
  Statefull object)
* Pre/Post transition support at the state machine level. Global hooks exist.
* Registration hooks

### Thread Safety

Much was done to make this project thread safe. However, due to not having
control on the stateful objects given to the context, very little can be made
about guarantees. However, you have the ability to add in a locking plugin model
to the context that can be used to help increase thread safety.

Below are details about thread safety of the internals.

#### State Machines

The building of state machines is thread safe. State machines themselves are
identified with three variables. Their name, a version and an id. The name is
given by the creator of the state machine. The version can be manually set in
the JSON or changes everytime a new state or transition is added/updated.
Finally, each state machine is identified when loaded into the JVM with a UUID.
The factories that manipulate state machine are using a functional programming
style to limit side-effects.

#### Context

The registering and registering state machines in the context is *not*
thread-safe as the mapping between classes, and their state machines is stored
in a regular HashMap. (This may change in the future.)
However, it is recommended to only add state machines during the setup phase.
This can be modified to be completely thread-safe should the need be there.

#### Hooks

Hooks and locking mechanism are being reworked to increase thread safety. Note
your hooks can always make things worse... so be mindful with your hooks. The
pluggable locking model is in rework as well.

#### Stateful objects

State changes on objects are typically not thread-safe. Depending on the use of
the pluggable locking mechanism in-play, you can make them thread safe. But be
aware, der be dragons here...