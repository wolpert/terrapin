# Terrapin

Secure 3rd party data management.

Requires Java 17 or higher.

## Description

The terrapin project provides the primary data owners the ability to let third
parties access their data safely. Primary owners can revoke access at any time,
removing the ability for a third party to access the data. During this time, the
data itself is store at the third party, encrypted via terrapin, and controlled
by the owner.

Internally, the terrapin project is a set of software projects that help provide
features that can be used for a broad range of applications. These features were
used internally for **CodeHead** projects, but have been made available for
external usages. When possible, features are developed in a functional
programming style, with limited side effects.

## Design

Terrapin is a suite of microservices with layers so changing out the datastore
or the execution environment has reduced complexity. Complexity of each service
is minimized and instead, new microservices are created for each function. I
considered a serverless environment by opted for a defaulted dropwizard
environment.

Datastore could be DynamoDB, Cassandra or SQL. However, I'll be focused
initially on Cassandra. A full test suite is available for testing out new
datastores.

In initial set of microservices will be published during the project. One
project that is planned on being used by not directly within terrapin is
VioletKeys; a data encryption layer written in Rust. It uses standard encryption
techniques but retrieves the keys from the keystore for encryption and
decryption. VioletKeys is design to handle identifying callers and client-side
identification. VioletKeys has the highest security threat model of the project.

## Building

Terrapin requires JDK17 or higher. I'm tired of not being able to upgrade Java.
Seriously, it's been years. I want var, record, better streams, optional, etc.


Copyright (c) 2022 Ned Wolpert <ned.wolpert@gmail.com>  
License: Apache 2.0