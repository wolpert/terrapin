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

package com.codeheadsystems.terrapin.server.dao.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

@Singleton
public class BatchWriteConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchWriteConverter.class);

    @Inject
    public BatchWriteConverter() {
        LOGGER.info("BatchWriteConverter()");
    }

    public BatchWriteItemRequest fromPutItemRequests(final PutItemRequest... requests) {
        final Map<String, ? extends Collection<WriteRequest>> items = Arrays.stream(requests)
                .map(PutItemRequest::tableName)
                .distinct()
                .collect(Collectors.toMap(t -> t, t -> new ArrayList<>()));
        for (PutItemRequest request : requests) {
            items.get(request.tableName()).add(toWriteRequest(request));
        }
        return BatchWriteItemRequest.builder()
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .requestItems(items)
                .build();
    }

    public Optional<BatchWriteItemRequest> unprocessedRequest(final BatchWriteItemResponse response) {
        if (response.hasUnprocessedItems() && response.unprocessedItems().size() > 0) {
            return Optional.of(BatchWriteItemRequest.builder()
                    .requestItems(response.unprocessedItems())
                    .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                    .build());
        } else {
            return Optional.empty();
        }
    }

    private WriteRequest toWriteRequest(final PutItemRequest putItemRequest) {
        return WriteRequest.builder()
                .putRequest(PutRequest.builder()
                        .item(putItemRequest.item())
                        .build())
                .build();
    }

}
