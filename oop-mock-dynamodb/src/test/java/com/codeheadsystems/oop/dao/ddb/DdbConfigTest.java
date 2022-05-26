// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dao.ddb;

import static org.junit.jupiter.api.Assertions.*;

import com.codeheadsystems.test.model.BaseJacksonTest;

class DdbConfigTest extends BaseJacksonTest<DdbConfig> {

    @Override
    protected Class<DdbConfig> getBaseClass() {
        return DdbConfig.class;
    }

    @Override
    protected DdbConfig getInstance() {
        return ImmutableDdbConfig.builder()
                .accessKey("accessx")
                .secretKey("secretx")
                .endpoint("endpointx")
                .build();
    }
}