
CREATE KEYSPACE
    IF NOT EXISTS keystore
    WITH replication={'class' : 'SimpleStrategy', 'replication_factor':1}
    AND durable_writes = false;