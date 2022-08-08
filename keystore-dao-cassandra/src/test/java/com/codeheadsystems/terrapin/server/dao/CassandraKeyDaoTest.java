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

package com.codeheadsystems.terrapin.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class CassandraKeyDaoTest {

  public static final String CASSANDRA_VERSION = "4.0.5";
  public static final String DATACENTER = "datacenter1";

  @Container
  public CassandraContainer<?> container = new CassandraContainer<>(DockerImageName.parse("cassandra")
      .withTag(CASSANDRA_VERSION))
      .withInitScript("test-keystore.cql");

  private CqlSession cqlSession() {
    final InetSocketAddress address =
        new InetSocketAddress(container.getHost(), container.getMappedPort(CassandraContainer.CQL_PORT));
    return CqlSession.builder().addContactPoint(address).withLocalDatacenter(DATACENTER).build();
  }

  @Test
  public void testSessions() {
    try (final CqlSession session = cqlSession()) {
      assertThat(session.getMetadata().getKeyspaces().values())
          .isNotEmpty();
    }
  }

}