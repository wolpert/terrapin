# Keystore Service

# Resources

## Build notes

### Logging
Logging change once we update to SLF4J 2.x. We needed to upgrade logback
as well. Dropwizard pulls in an older version of logback so we added to
also include the logback-access project. But then we don't include the
other api libs needed by dropwizard which are used for logback-access
itself. So we needed both the `javax.servlet-api` and `jarkarta.servlet-api`
packages as well. See `keystore-integ-tests/build.gradle` for details.