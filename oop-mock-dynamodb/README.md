# MockDataDAO for DynamoDB

## Usage

In order to get the DAO to function, you must provide it with a 
DynamoDBMapper. If you are using an IoC framework like Dagger or
guice, you just add the DynamoDBMapper to the context. It's up to
you to get the authentication for the mapper setup correctly.