# MockDataDAO for DynamoDB

Provides the ability to store in Amazon's DynamoDB the mocked data for OopMock
calls.

## Configuring

### oopMockConfiguration.json

This file, of course, should be in your path. A minimal configuration would look
like

```json
{
  "enabled": "true",
  "resolverConfiguration": {
    "resolverClass": "com.codeheadsystems.oop.dao.ddb.MockDataDDBDAO"
  }
}
```

### DynamoDB Mapper

For the code to work, it needs a configured DynamoDB Mapper. Instead of adding
credential data to the resolver configuration, it was decided that it was just
easier to have the user setup a DynamoDB mapper object as needed, and pass it
into the factory builder.

* OopMockFactory

```java
OopMockFactoryBuilder.generate(ImmutableMap.of(DynamoDBMapper.class, mapper));`
```

* OopMockClientFactory

```java
OopMockClientFactoryBuilder.generate(ImmutableMap.of(DynamoDBMapper.class, mapper));`
```

### Database

You can configure your DynamoDB database manually through the scripting
mechanism of your choice. (CDK, Terraform, Pulumi, etc)

Currently, the names are hard-coded. They include:

* TableName: oop_mock_data
* HashKey: hash
* RangeKey: range
* TTL: ttl
  (TTL is not currently set)