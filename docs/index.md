# terrapin

> **NOTICE** The terrapin project is morphing. I'm starting to include new or unfinished development
> work into the project from CodeHead system. CodeHead is officially dead as a company.
> The terrapin project is a redo of the original Method System work from Colorado.
> Just open-sourced this time.

# About

Terrapin is a collection of software being open-sourced that was used within CodeHead Systems.
The work is licensed under the Apache 2.0 license to promote being used by others with as much freedom
as possible... commercial or otherwise.

## Out-of-process mock

[OopMock](https://github.com/wolpert/terrapin/tree/main/oop-mock) is a way to configure
services so that you can mock their downstream dependencies as configured by a test client
that runs 'out-of-process'. The use-case is this: Using a functional test client, configure
how your service dependency will behave when called with specific inputs. Then your 
functional test client calls the service and has expected results.  It makes testing
error conditions easier and when calls to downstream clients are destructive.

## State Machine

[StateMachine](https://github.com/wolpert/terrapin/tree/main/statemachine) provides a general-purpose
state machine with both programmatic and JSON-loading support for state machine definitions. Stateful
objects can use annotations for fields that encompasses the state. It provides metricsImplementation, pluggable locking
mechanism and pre/post hooks as well. 

## CodeHead-Test

[CodeHeadTest](https://github.com/wolpert/terrapin/tree/main/codehead-test) is a selection of test utilities
used internally within CodeHead. These are being release as other code released under terrapin use them.
Note datastore-test was created to set up different datastores during unit tests under
jUnit5.

# History

As CodeHead Systems was winding down, I wanted to do something with the software that 
was not specific to any project. So I started Terrapin as a way to get the software into
open-source under the Apache 2.0 license. But in the back of my head I wanted to still
work on the key/data management project... so I'm renaming that project Terrapin.

---
