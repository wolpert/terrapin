# DynamoDB DAO

DAO as implemented for DynamoDB

## Database Model

We are only using one table.
* Key hashkeys = keyVersion:%s:%s (first is owner name, second is key name )
* Key rangeKeys are versions. (As strings, zero-padded with 10 zeros)
* Key's have sparse index for active keys and owners.

* Owners hashkeys = owner:%s where the owner name is added.
* Owner range keys = 'key:%s' where the key name is added.
* Owners range key = 'info' for the base owner row.
