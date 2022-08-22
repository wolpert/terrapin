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

package com.codeheadsystems.keystore.integ.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.codeheadsystems.keystore.api.Key;
import com.codeheadsystems.keystore.api.KeyReaderService;
import com.codeheadsystems.keystore.common.factory.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientFactoryTest {

  private static ObjectMapper objectMapper;
  private static String connectionUrl;

  private ClientFactory clientFactory;

  @BeforeAll
  public static void setup() {
    objectMapper = new ObjectMapperFactory().generate();
    connectionUrl = "http://localhost:8080/";
  }

  @BeforeEach
  void setupClient() {
    clientFactory = new ClientFactory(connectionUrl, objectMapper);
  }

  @Test
  public void testReaderImpl() {
    final KeyReaderService service = clientFactory.keyReaderService();
    assertThat(service)
        .isNotNull();
  }

  //@Test
  public void testGet() {
    final Key key = clientFactory.keyReaderService()
        .get("One", "two", 3L);
  }

}