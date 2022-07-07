# Terrapin

A collection of open-source tools.

## Description

The Terrapin project is a set of software projects that help provide features that can be used for a broad range
of applications. These features were used internally for **CodeHead** projects, but have been made available for
external usages. When possible, features are developed in a functional programming style, with limited side
effects.

## Projects

### StateMachine

Provides a general purpose state machine implementation with pre/post transition hooks, functional styling and
metricsImplementation. It proved pretty useful in older personal projects so made it open-source to increase availability.

### CodeHead Test

This was originally a dumping ground of test utilities. Many of the utilities had become redundant lately, so
I'm picking and choosing what to add here if they don't exist elsewhere.
I'll move more test utilities in as it makes sense.

### OopMock

Out-of-process mock provides the ability to set mocks up for functional tests that work against service
in an integration pattern. So if you are testing from the outside calls to your service
but want to mock out client calls happening downstream of your service, this is for you. It
has a safety value so that in production (where there is no configuration) the service
is disabled completely.

This is actually not an older project of mine... rather I built this after realizing how much
I missed the internal Amazon project 'Chameleon' which OopMock was based on.

Copyright (c) 2022 Ned Wolpert <ned.wolpert@gmail.com>  
License: Apache 2.0