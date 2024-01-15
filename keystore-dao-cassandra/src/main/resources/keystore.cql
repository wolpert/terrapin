-- noinspection SqlNoDataSourceInspectionForFile

CREATE KEYSPACE
    IF NOT EXISTS keystore
    WITH replication ={'class' : 'SimpleStrategy', 'replication_factor':1}
     AND durable_writes = false;

CREATE TABLE
    IF NOT EXISTS
    keystore.owners
(
    owner       text,
    lookup      text,
    type        text,
    create_date timestamp,
    PRIMARY KEY ( (owner), lookup )
);

CREATE INDEX
    IF NOT EXISTS
    owners_lookup_idx
    on keystore.owners (lookup);

CREATE TABLE
    IF NOT EXISTS
    keystore.keys
(
    owner       text,
    key_name    text,
    version     bigint,
    value       blob,
    aux         blob,
    active      boolean,
    type        text,
    create_date timestamp,
    update_date timestamp,
    PRIMARY KEY ((owner, key_name), version)
) with CLUSTERING ORDER BY (version desc);

CREATE TABLE
    IF NOT EXISTS
    keystore.active_keys
(
    owner       text,
    key_name    text,
    version     bigint,
    value       blob,
    aux         blob,
    active      boolean,
    type        text,
    create_date timestamp,
    update_date timestamp,
    PRIMARY KEY ((owner, key_name), version)
) with CLUSTERING ORDER BY (version desc);
