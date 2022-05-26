// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Contains mocked data.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMockedData.class)
@JsonDeserialize(builder = ImmutableMockedData.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface MockedData {

    String marshalledData();

    @Value.Default
    default long delayInMS() {
        return 0;
    }

}
