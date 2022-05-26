// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.dao.ddb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDdbConfig.class)
@JsonDeserialize(builder = ImmutableDdbConfig.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface DdbConfig {

    @JsonProperty("amazon.dynamodb.endpoint")
    String endpoint();

    @JsonProperty("amazon.aws.accesskey")
    String accessKey();

    @JsonProperty("amazon.aws.secretkey")
    String secretKey();

}
