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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrchestratorTest {

    private Database database;
    private Client client;
    private Server server;

    @BeforeEach
    void setup() {
        database = new Database();
        client = new Client();
        server = new Server();

        database.setup();
        client.setup(database);
        server.setup(database);
    }

    @AfterEach
    void teardown() {
        server.teardown();
        client.teardown();
        database.teardown();
    }

    @Test
    public void callTest() {
        assertThat(client.callServerNotMocked(server))
                .isEqualTo(Server.BASE_RESULT);
    }

    @Test
    public void notMockedTest() {
        assertThat(client.callServerNotMockedByUs(server))
                .isEqualTo(Server.BASE_RESULT);
    }

    @Test
    public void mockTest() {
        assertThat(client.callServerMocked(server))
                .isEqualTo(Client.MOCKED_DATA);
    }

}