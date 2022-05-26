// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableOopMockConfiguration.class)
@JsonDeserialize(builder = ImmutableOopMockConfiguration.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface OopMockConfiguration {

    /**
     * Is everything just enabled.
     *
     * @return boolean
     */
    @Value.Default
    @JsonProperty("enabled")
    default boolean enabled() {
        return false;
    }

    /**
     * Delays are settable with the mocked data. By default, we do not use them.
     *
     * @return boolean if the delay should be used.
     */
    @Value.Default
    @JsonProperty("delayResponseEnabled")
    default boolean delayResponseEnabled() {
        return false;
    }

    /**
     * If mocked data is from a file, you can set this here.
     * WARNING, this will likely change format later.
     *
     * @return optional string.
     */
    @JsonProperty("mockDataFileName")
    Optional<String> mockDataFileName();

}
