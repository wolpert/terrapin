/*
 *    Copyright (c) 2022 Ned Wolpert <ned.wolpert@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.codeheadsystems.oop.test;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.codeheadsystems.oop.client.OopMockClient;
import com.codeheadsystems.oop.client.OopMockClientFactory;
import com.codeheadsystems.oop.client.dagger.OopMockClientFactoryBuilder;
import com.google.common.collect.ImmutableMap;

public class Client {
    public static final String MOCKED_DATA = "this is mocked data";

    private OopMockClientFactory factory;

    public void setup(Database database) {
        factory = OopMockClientFactoryBuilder.generate(ImmutableMap.of(DynamoDBMapper.class, database.dynamoDBMapper()));
    }

    public String callServerNotMocked(final Server server) {
        final OopMockClient client = factory.generate(Client.class);
        return server.getBaseResult("callServerNotMocked");
    }

    public String callServerNotMockedByUs(final Server server) {
        final OopMockClient client = factory.generate(Server.class);
        client.mockSetup(MOCKED_DATA, "getBaseResult", "callServerMocked");
        try {
            return server.getBaseResult("callServerNotMockedByUs");
        } finally {
            client.deleteMock("getBaseResult", "callServerMocked");
        }
    }

    public String callServerMocked(final Server server) {
        final OopMockClient client = factory.generate(Server.class);
        client.mockSetup(MOCKED_DATA, "getBaseResult", "callServerMocked");
        try {
            return server.getBaseResult("callServerMocked");
        } finally {
            client.deleteMock("getBaseResult", "callServerMocked");
        }
    }

    public void teardown() {
        factory = null;
    }

}
