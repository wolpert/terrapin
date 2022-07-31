# Out of process Mock

## Purpose

Provide a mechanism for external test clients to setup a mock such that
when the test client calls the service, the service will use the mock
instead of the intended target. Commonly used to mock out dependencies
of the service under test.

## Process

At its core, OopMock gets the signature of the method being mocked via the
class/method name, with the arguments as the discriminator. If it finds a
match of that signature/discriminator, it will convert stored data and
return the response. In the event they are not found, it will simply call
the method.

The intent is that this can be extended to OpenAPI clients, OpenFeign or
other clients so that this can be invisible to the user of the library.
But this provides the basic building blocks.

**Caveat**: The default behavior shown here requires the test client to
have the class model of the server. Extensions to OopMock will remove this
requirement.

The server code that needs to be mocked is instrumented with the OopMock
proxy. Example

Before:

```java
public String getInternalData(String id) {
    final String request = "/v1/dataset/" + id;
    final Response r = client.call(request);
    return r.getData();
}
```

After

```java
private final OopMock oopMock = oopMockFactory.generate(getClass());
public String getInternalData(String id) {
    return oopMock.proxy(String.class, () ->{
            final String request = "/v1/dataset/"+id;
            final Response rdf = client.call(request);
            return r.getData();
          },"getInternalData",id);
}
```

Client code can then setup the proxy:

```java
final String mockResult = "Something wicked this way comes";
final OopMockClient mock = oppMockClientFactory.generate(ServerClass.class);
mock.setup(mock.hash("getInternalData","55432"), mockResult);
final ServerResult result = server.call("id:55432");
assertThat(result.getData()).isEqualTo(mockResult);
```

## Creating Factories

OopMockFactory and OopMockClientFactory are thread-safe instances that can be reused and
treated like singletons. You should inject them with the IoC framework of your choice.

They are built using the OopMockFactoryBuilder (or OopMockClientFactoryBuilder) with the same
parameters. (For simplicity, I'll just review the OopMockFactoryBuilder going forward, but the rules
apply for both.)

To function, you need a configuration file in your classpath named oopMockConfiguration.json that
at a minimum states that the OomMock is enabled, like so:

```json
{
  "enabled": "true"
}
```

If no file exists, OopMock will run in disabled mode. There is virtual no impact to runtime
performance of your server when it is disabled. When enabled, it requires the setting of a
resolver (like oop-mock-dynamodb) which will require at least updates to the configuration file.
(Check your resolver for how to use)

Out of the box, OopMock provides the ability for an InMemory resolver, and no client is used.
This can be useful if you have static data you want for mocks that never changes. To use the
InMemory resolver, your configuration file would look like this:

```json
{
  "enabled": "true",
  "mockDataFileName": "testData.json"
}
```

Where testData.json is the test data itself. (See the InMemory format for details)

## Namespace

Any Resolver Datastore (InMemory, DynamoDB, JDBC, etc) can contain data needed for multiple
servers. Every Mock request has a 'namespace' associated with it. By default, it's set to ```DEFAULT```

You can set the namespace in your configuration file instead. It's recommended to leave it
and only set it when you need multiple namespaces.

```json
{
  "enabled": "true",
  "mockDataFileName": "testData.json",
  "namespace": "My Application"
}
```

## FAQ

### Is the mocked code executed in production environments?

The intent is for the default behavior to provide a pass-thru for the
client. To enable the OopMock framework, the factory must be provided
the appropriate argument on creation. By default, it's disabled. Your
runtime environment should enable this argument the same way it enables
debugging ports.

### What is the increased latency for the pass thru mode?

In pass thru mode, the framework is disabled and there is no latency impact.