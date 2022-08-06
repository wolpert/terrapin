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

import com.codeheadsystems.oop.client.OopMockClient;
import com.codeheadsystems.oop.client.OopMockClientFactory;

public class Client {
  public static final String MOCKED_DATA = "this is mocked data";

  private final OopMockClientFactory factory;

  public Client(final OopMockClientFactory factory) {
    this.factory = factory;
  }

  /**
   * Test: We generate the OopMockClient, but don't use it.
   * Expected result would be un-mocked data.
   *
   * @param server we are calling.
   * @return data from the server.
   */
  public String callServerWithoutMock(final Server server, final String id) {
    return server.getBaseResult(id);
  }

  /**
   * Test: We generate the OopMockClient, setup a mock, but use a different ID
   * on the request than what we mocked.
   * Expected result would be un-mocked data.
   *
   * @param server we are calling.
   * @return data from the server.
   */
  public String callServerWithMockOnDifferentId(final Server server, final String id) {
    final OopMockClient client = factory.generate(Server.class);
    final String mockId = id + " other value";
    client.mockSetup(MOCKED_DATA, Server.LOOKUP, mockId);
    try {
      return server.getBaseResult(id);
    } finally {
      client.deleteMock(Server.LOOKUP, mockId);
    }
  }

  /**
   * Test: We generate the OopMockClient, setup a mock, and use the same ID
   * on the request than what we mocked.
   * Expected result would be mocked data.
   *
   * @param server we are calling.
   * @return data from the server.
   */
  public String callServerMocked(final Server server, final String id) {
    final OopMockClient client = factory.generate(Server.class);
    client.mockSetup(MOCKED_DATA, Server.LOOKUP, id);
    try {
      return server.getBaseResult(id);
    } finally {
      client.deleteMock(Server.LOOKUP, id);
    }
  }

}
