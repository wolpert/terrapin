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

package com.codeheadsystems.keystore.server.dao;

import com.codeheadsystems.keystore.server.dao.casssandra.dagger.CqlSessionModule;
import com.datastax.oss.driver.api.core.CqlSession;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.utility.DockerImageName;

class CassandraKeyDaoTest extends KeyDaoTest {
  public static final String CASSANDRA_VERSION = "4.0.5";
  public static final String KEYSTORE_CQL = "keystore.cql";
  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraKeyDaoTest.class);
  public static Retry retry;
  public static CassandraContainer<?> container;
  private static CqlSession cqlSession;
  private static KeyDao keyDao;

  @BeforeAll
  public static void setupRetry() {
    final RetryRegistry registry = RetryRegistry.ofDefaults();
    retry = registry.retry("retry.CassandraKeyDaoTest");
    TaggedRetryMetrics.ofRetryRegistry(registry)
        .bindTo(meterRegistry);
  }

  @BeforeAll
  public static void setupContainer() {
    container = new CassandraContainer<>(DockerImageName.parse("cassandra")
        .withTag(CASSANDRA_VERSION))
        .withInitScript(KEYSTORE_CQL);
    container.start();
    final InetSocketAddress address =
        new InetSocketAddress(container.getHost(), container.getMappedPort(CassandraContainer.CQL_PORT));
    final DaoComponent component = DaggerDaoComponent.builder()
        .cqlSessionModule(new CqlSessionModule(address))
        .ourMeterModule(new DaoComponent.OurMeterModule(meterRegistry))
        .build();
    cqlSession = component.cqlSession();
    keyDao = component.keyDao();
  }

  @AfterAll
  public static void removeContainer() {
    container.stop();
    container = null;
  }

  @Override
  protected KeyDao keyDAO() {
    return keyDao;
  }
}