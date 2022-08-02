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
import java.util.Arrays;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraExtension extends DataStoreExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraExtension.class);

    private static final Class<?> SESSION = CqlSession.class;

    @Override
    protected Class<?> namespaceClass() {
        return CassandraExtension.class;
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        LOGGER.info("Tearing down in memory DynamoDB local instance");
        withStore(context, s -> {
            s.remove(SESSION, CqlSession.class);
        });
        try {
            EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        super.beforeEach(context);
        withStore(context, store -> {
            context.getRequiredTestInstances().getAllInstances().forEach(o -> {
                Arrays.stream(o.getClass().getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(Keyspace.class))
                        .forEach(field -> {
                            enableSettingTheField(field);
                            try {
                                final String keyspace = field.get(o).toString();
                                if (store.get(keyspace) == null) {
                                    store.put(keyspace, keyspace);
                                    new CQLDataLoader(store.get(SESSION, CqlSession.class))
                                            .load(new ClassPathCQLDataSet(keyspace + ".cql", keyspace));
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        });
            });
        });
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        LOGGER.info("Setting in memory Cassandra instance");

        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        final CqlSession session = EmbeddedCassandraServerHelper.getSession();
        withStore(context, s -> {
            s.put(SESSION, session);
        });
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
