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

package com.codeheadsystems.terrapin.server.dao.dagger;

import com.codeheadsystems.metrics.MetricsHelper;
import com.codeheadsystems.terrapin.server.dao.DynamoDbClientAccessor;
import com.codeheadsystems.terrapin.server.dao.KeyDAO;
import com.codeheadsystems.terrapin.server.dao.KeyDAODynamoDB;
import com.codeheadsystems.terrapin.server.exception.RetryableException;
import dagger.Module;
import dagger.Provides;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import javax.inject.Named;
import javax.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Module(includes = DDBModule.Binder.class)
public class DDBModule {

    public static final String DDB_DAO_RETRY = "DDB_DAO_RETRY";

    @Named(DDB_DAO_RETRY)
    @Provides
    @Singleton
    public Retry retry() {
        final RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .retryExceptions(RetryableException.class)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(100, 2))
                .failAfterMaxAttempts(true)
                .build();
        final RetryRegistry registry = RetryRegistry.of(config);
        return registry.retry(DDB_DAO_RETRY);
    }

    @Provides
    @Singleton
    public DynamoDbClientAccessor accessor(@Named(DDB_DAO_RETRY) final Retry retry,
                                           final DynamoDbClient client,
                                           final MetricsHelper metricsHelper) {
        return new DynamoDbClientAccessor(client, metricsHelper, retry);
    }

    @Module
    public interface Binder {

        KeyDAO dao(KeyDAODynamoDB dao);

    }

}
