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

package com.codeheadsystems.test.datastore;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.CassandraContainer;

/**
 * Sets up the cassandra instance.
 */
public class CassandraExtension
    extends DataStoreExtension
    implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraExtension.class);

  private static final Class<?> SESSION = CqlSession.class;
  private final ArrayList<String> keyspaces = new ArrayList<>();
  private CassandraContainer<?> cassandraContainer;
  private long startTime;

  @Override
  protected Class<?> namespaceClass() {
    return CassandraExtension.class;
  }

  @Override
  public void beforeAll(final ExtensionContext context) throws Exception {
    LOGGER.info("Setting in memory Cassandra instance");
    startTime = System.currentTimeMillis();
    cassandraContainer = new CassandraContainer<>("cassandra:4.0.5");
    cassandraContainer.start();
    final CqlSession session = CqlSession
        .builder()
        .addContactPoint(
            new InetSocketAddress(
                cassandraContainer.getHost(),
                cassandraContainer.getMappedPort(CassandraContainer.CQL_PORT)))
        .withLocalDatacenter("datacenter1")
        .build();
    withStore(context, s -> {
      s.put(SESSION, session);
    });
    LOGGER.info("Embedded cassandra setup");
  }

  @Override
  public void afterAll(final ExtensionContext context) {
    LOGGER.info("Tearing down in memory DynamoDB local instance");
    withStore(context, s -> {
      try (CqlSession session = s.remove(SESSION, CqlSession.class)) {
        keyspaces.forEach(keyspace -> session.execute("DROP KEYSPACE IF EXISTS " + keyspace));
      } catch (RuntimeException re) {
        re.printStackTrace();
      }
      cassandraContainer.stop();
    });
    cassandraContainer = null;
    LOGGER.info("Total time(ms): {} for {}", System.currentTimeMillis() - startTime, context.getDisplayName());
  }

  @Override
  public void beforeEach(final ExtensionContext context) {
    super.beforeEach(context);
    LOGGER.info("start beforeEach:{}", context.getDisplayName());
    withStore(context, store -> {
      context.getRequiredTestInstances().getAllInstances().forEach(o -> {
        Arrays.stream(o.getClass().getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(Keyspace.class))
            .forEach(field -> {
              enableSettingTheField(field);
              try {
                final String keyspace = field.get(o).toString();
                if (!keyspaces.contains(keyspace)) {
                  new CQLDataLoader(store.get(SESSION, CqlSession.class))
                      .load(new ClassPathCQLDataSet(keyspace + ".cql", keyspace));
                  keyspaces.add(keyspace);
                }
              } catch (Throwable e) {
                e.printStackTrace();
              }
            });
      });
    });
    LOGGER.info("end beforeEach:{}", context.getDisplayName());
  }

  @Override
  public boolean supportsParameter(final ParameterContext parameterContext,
                                   final ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.isAnnotated(DataStore.class);
  }

  @Override
  public Object resolveParameter(final ParameterContext parameterContext,
                                 final ExtensionContext extensionContext) throws ParameterResolutionException {
    return extensionContext.getStore(namespace).get(SESSION);
  }


}
