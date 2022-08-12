# Cassandra DAO

Working the cassandra DAO is almost orthogonal to the DynamoDB one. DynamoDB
wants one table if possible, global secondary indexes on fields that change is
fine as long as you can stand eventual consistency, and the cardinality of those
fields in the index is fine if you get enough variability between with the
hash/range key.

Cassandra is built for writes. New data is added quickly. You have potentially
more issues reading a new entry just written than you do with DynamoDB. Forcing
high consistency works for DynamoDB reads at a time/money cost, but can be done
on the query level. Cassandra is done on the keyspace level, or using a backoff
policy in reads.

## Issues

There is the need to create a table of active keys. Secondary index on the active
field is not recommended  due to the low cardinality and potential high churn 
rate as we expect keys to be rotated at least monthly if not daily. The 
mechanism within DynamoDB or plain-sql works better for data that can change routinely. 

Instead of indexing, there will be two keys tables; one for all non-deleted keys
and one for active keys only. Lookups that are given the full versioned key go
to the regular keys tables. Lookups for the latest active key searches the active
table. 

## What I didn't pick

Decided against a materialized view since its still experimental within
Cassandra... but that would be the obvious next step.

Decided against having the base 'key' contain a set of active versions as it
results in multiple reads from the table for one key.  (One read for the base,
and one for the version.)

## Other notes

I'm still using the 'owners' table as a super-table. Meaning it has owner
details in addition to owners' keys. (Without versions) This table will change
the least over time. Because of this, the compaction strategy between owners
and the two keys tables will differ.

Finally, may need a self-healing technique with deactivated keys. We still store
the active state on each key. Considering an async process when deactivate keys
are referenced.