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
private final OopMock oopMock = OopMockFactory.generate(getClass());
public String getInternalData(String id) {
    return oopMock.proxy(String.class, () ->{
            final String request = "/v1/dataset/"+id;
            final Response rdf = client.call(request);
            return r.getData();
          },"getRemoveData",id);
}
```

Client code can then setup the proxy:
```java
final String mockResult = "Something wicked this way comes";
final OopMockClient mock = OppMockClientFactory.generate(ServerClass.class);
mock.setup(mock.hash("getRemoveData","55432"), mockResult);
final ServerResult result = server.call("id:55432");
assertThat(result.getData()).isEqualTo(mockResult);
```

## FAQ

### Is the mocked code executed in production environments?
The intent is for the default behavior to provide a pass-thru for the
client. To enable the OopMock framework, the factory must be provided
the appropriate argument on creation. By default it's disabled. Your
runtime environment should enable this argument the same way it enables
debugging ports.

### What is the increased latency for the pass thru mode?
In pass thru mode, the framework is disabled and there is no latency impact.