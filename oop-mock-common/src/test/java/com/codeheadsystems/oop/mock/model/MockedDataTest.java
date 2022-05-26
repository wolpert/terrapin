// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.model;

import com.codeheadsystems.test.model.BaseJacksonTest;

class MockedDataTest extends BaseJacksonTest<MockedData> {

    @Override
    protected Class<MockedData> getBaseClass() {
        return MockedData.class;
    }

    @Override
    protected MockedData getInstance() {
        return ImmutableMockedData.builder()
                .marshalledData("stuff")
                .build();
    }


}