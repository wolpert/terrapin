# terrapin

terrapin is a project to give the primary data owners the ability to let third parties access their
data safely. Primary owners can revoke access at any time, removing the ability for a third party
to access the data. During this time, the data itself is store at the third party, encrypted via
terrapin.

Under the covers, terrapin is a collections of independent projects and micro-services that can
serve other purposes beyond the primary purpose of the terrapin project. These subprojects as well as
the primary project are all licensed under the permissive Apache 2 license scheme.

As of right now, the software exists in a mono-repository to ease the development process. Changes
to the subprojects must not break the other projects that rely on them. Using a mono-repository
forces execution of all the tests to ensure compatibility.

# About Subprojects

These subprojects were developed to provide specific features that were reusable across software
within CodeHead Systems. THe original projects and new ones being developed are all open-sourced.

## Key Store

Key Store (KeyService) is a micro-Service that provides the ability create, store and
rotate keys. They can be shared, enabled and disabled. Key generation has a pluggable
model for random number generator. (For our purposes, we only use SecureRandom from
Java by default.)

## Out-of-process mock

[OopMock](https://github.com/wolpert/terrapin/tree/main/oop-mock) is a way to configure
services so that you can mock their downstream dependencies as configured by a test client
that runs 'out-of-process'. The use-case is this: Using a functional test client, configure
how your service dependency will behave when called with specific inputs. Then your
functional test client calls the service and has expected results. It makes testing
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
work on the key/data management project... so I'm renaming that project terrapin.

---
