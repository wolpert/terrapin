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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.keystore.Server;
import com.codeheadsystems.keystore.api.Key;
import com.codeheadsystems.keystore.api.KeyReaderService;
import com.codeheadsystems.keystore.common.factory.ObjectMapperFactory;
import com.codeheadsystems.keystore.config.KeyStoreConfiguration;
import com.codeheadsystems.test.unique.UniqueString;
import com.codeheadsystems.test.unique.UniqueStringExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import java.util.Random;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(UniqueStringExtension.class)
class ClientFactoryTest {

  public static final String CASSANDRA_VERSION = "4.1.3";
  public static final String KEYSTORE_CQL = "keystore.cql";
  private static ObjectMapper objectMapper;
  private static String connectionUrl;
  private static CassandraContainer<?> container;
  private static DropwizardTestSupport<KeyStoreConfiguration> SUPPORT;

  private ClientFactory clientFactory;
  @UniqueString(prefix = "owner", separator = "-")
  private String owner;

  @BeforeAll
  public static void setup() throws Exception {
    container = new CassandraContainer<>(DockerImageName.parse("cassandra")
        .withTag(CASSANDRA_VERSION))
        .withInitScript(KEYSTORE_CQL);
    container.start();
    final int mappedPort = container.getMappedPort(CassandraContainer.CQL_PORT);
    SUPPORT = new DropwizardTestSupport<>(
        Server.class,
        ResourceHelpers.resourceFilePath("dev-cassandra-config.yaml"),
        ConfigOverride.config("dataStore.  connectionUrl", "http://localhost:" + mappedPort)
    );
    SUPPORT.before();
    objectMapper = new ObjectMapperFactory().generate();
    connectionUrl = "http://localhost:" + SUPPORT.getLocalPort() + "/";
  }

  @AfterAll
  public static void shutdown() {
    container.stop();
    SUPPORT.after();
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

  @Test
  public void testGet_notfound() {
    assertThatExceptionOfType(FeignException.class)
        .isThrownBy(() -> clientFactory.keyReaderService().get("One", "two", 3L))
        .withMessageContaining("404");
  }

  @Test
  public void testCreateGet() {
    final Key key = clientFactory.keyManagerService().create(owner, "testCreateGet");
    final Key result = clientFactory.keyReaderService().get(key.owner(), key.id(), key.version());
    assertThat(key)
        .isEqualTo(result);
  }

  @Test
  public void testCreateGetWithoutVersion() {
    final Key key = clientFactory.keyManagerService().create(owner, "testCreateGetWithoutVersion");
    final Key result = clientFactory.keyReaderService().get(key.owner(), key.id());
    assertThat(key)
        .isEqualTo(result);
  }

}