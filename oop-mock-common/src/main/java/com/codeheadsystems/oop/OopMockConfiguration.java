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

    /**
     * Will use the resolver configuration as defined. Note that pulling in a resolver
     * is done by adding in a resolver module to the dagger ioc. This allows for runtime.
     * configuration.
     *
     * @return resolver configuration.
     */
    @JsonProperty("resolverConfiguration")
    Optional<ResolverConfiguration> resolverConfiguration();

}
