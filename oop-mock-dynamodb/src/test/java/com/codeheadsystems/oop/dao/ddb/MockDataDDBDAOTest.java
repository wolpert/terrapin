// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dao.ddb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.codeheadsystems.oop.dao.ddb.converter.DDBEntryConverter;
import com.codeheadsystems.oop.dao.ddb.model.DDBEntry;
import com.codeheadsystems.oop.mock.model.MockedData;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * This is half unit test and have functional in that we use a real (local) ddb instance.
 * I'm just really tired of cases where people ignore how the database works until integ
 * tests. So forcing the issue here.
 *
 * Don't worry, the DDB instance is fast. But you'll need to load the SQL lib into your
 * path for now to get it to work. See DynamoDBExtension for details. (Only needed for
 * intelij, not for gradle on the cmdline.)
 */
@ExtendWith({MockitoExtension.class, DynamoDBExtension.class})
class MockDataDDBDAOTest {

    public static final String NAMESPACE = "namespace";
    public static final String LOOKUP = "lookup";
    public static final String DISCRIMINATOR = "discriminator";
    public static final String HASH = "a";
    public static final String RANGE = "b";
    public static final DDBEntry ENTRY_WITHOUT_DATA = new DDBEntry(HASH, RANGE);
    public static final String MOCK_DATA = "c";
    public static final DDBEntry ENTRY_WITH_DATA = new DDBEntry(HASH, RANGE, MOCK_DATA);
    private MockDataDDBDAO dao;

    @LocalDynamoDB private DynamoDBMapper mapper;
    @LocalDynamoDB private AmazonDynamoDB amazonDynamoDB;
    @Mock private MockedData mockedData;
    @Mock private DDBEntryConverter converter;
    @Captor private ArgumentCaptor<DDBEntry> ddbEntryCaptor;

    @BeforeEach
    void setup() {
        dao = new MockDataDDBDAO(mapper, converter);
        amazonDynamoDB.createTable(mapper.generateCreateTableRequest(DDBEntry.class)
                .withBillingMode(BillingMode.PAY_PER_REQUEST));
    }

    @AfterEach
    void tearDown() {
        // force the table empty
        amazonDynamoDB.deleteTable(mapper.generateDeleteTableRequest(DDBEntry.class));
    }

    @Test
    void resolve_doesnotexist() {
        when(converter.convert(NAMESPACE, LOOKUP, DISCRIMINATOR)).thenReturn(ENTRY_WITHOUT_DATA);
        assertThat(dao.resolve(NAMESPACE, LOOKUP, DISCRIMINATOR))
                .isNotNull()
                .isEmpty();
    }

    @Test
    void resolve_exist() {
        when(converter.convert(NAMESPACE, LOOKUP, DISCRIMINATOR)).thenReturn(ENTRY_WITHOUT_DATA);
        when(converter.toMockedData(ddbEntryCaptor.capture())).thenReturn(Optional.of(mockedData));
        mapper.save(ENTRY_WITH_DATA);
        assertThat(dao.resolve(NAMESPACE, LOOKUP, DISCRIMINATOR))
                .isNotNull()
                .isNotEmpty()
                .contains(mockedData);
    }

    @Test
    void resolve_exist_butnomockeddata() {
        when(converter.convert(NAMESPACE, LOOKUP, DISCRIMINATOR)).thenReturn(ENTRY_WITHOUT_DATA);
        when(converter.toMockedData(ddbEntryCaptor.capture())).thenReturn(Optional.empty());
        mapper.save(ENTRY_WITH_DATA);
        assertThat(dao.resolve(NAMESPACE, LOOKUP, DISCRIMINATOR))
                .isNotNull()
                .isEmpty();
    }

    @Test
    void store() {
        when(converter.convert(NAMESPACE, LOOKUP, DISCRIMINATOR, mockedData))
                .thenReturn(ENTRY_WITH_DATA);

        dao.store(NAMESPACE, LOOKUP, DISCRIMINATOR, mockedData);

        assertThat(mapper.load(ENTRY_WITHOUT_DATA))
                .isNotNull()
                .hasFieldOrPropertyWithValue("mockData", MOCK_DATA);
    }

    @Test
    void delete_exists() {
        mapper.save(ENTRY_WITH_DATA);
        assertThat(mapper.load(ENTRY_WITHOUT_DATA))
                .isNotNull()
                .hasFieldOrPropertyWithValue("mockData", MOCK_DATA); // verify we got this
        when(converter.convert(NAMESPACE, LOOKUP, DISCRIMINATOR)).thenReturn(ENTRY_WITHOUT_DATA);

        dao.delete(NAMESPACE, LOOKUP, DISCRIMINATOR);
        assertThat(dao.resolve(NAMESPACE, LOOKUP, DISCRIMINATOR))
                .isNotNull()
                .isEmpty();
    }
    @Test
    void delete_doesnotexists() {
        when(converter.convert(NAMESPACE, LOOKUP, DISCRIMINATOR)).thenReturn(ENTRY_WITHOUT_DATA);

        dao.delete(NAMESPACE, LOOKUP, DISCRIMINATOR);
        assertThat(dao.resolve(NAMESPACE, LOOKUP, DISCRIMINATOR))
                .isNotNull()
                .isEmpty();
        // really we are asserting there is no exception.
    }

}