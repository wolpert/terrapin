/*
 *    Copyright (c) 2021 Ned Wolpert <ned.wolpert@gmail.com>
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

package com.codeheadsystems.test.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class StandardImmutableModelTest extends BaseJacksonTest<StandardImmutableModel> {

    @Override
    protected Class<StandardImmutableModel> getBaseClass() {
        return StandardImmutableModel.class;
    }

    @Override
    protected StandardImmutableModel getInstance() {
        return ImmutableStandardImmutableModel.builder()
                .addBunchOfString("string1", "string2")
                .someInt(5)
                .someString("this string")
                .nullableString("nullable string")
                .optionalString(Optional.of("a optional string"))
                .putAMap("a", "b")
                .putAMap("c", "d")
                .build();
    }

    /**
     * This test verifies the core BaseJacksonTest finds not null methods correctly.
     * Users of BaseJacksonTest do not need to implement this.
     */
    @Test
    public void testNotNullMethodNamess() {
        // Arrange
        final List<String> names = getRequiredMethods()
                .stream()
                .map(Method::getName)
                .collect(Collectors.toList());

        // Assert
        assertThat(names)
                .containsExactlyInAnyOrderElementsOf(ImmutableList.of("someInt", "someString"));
    }

    /**
     * This test verifies the core BaseJacksonTest finds collection methods correctly.
     * Users of BaseJacksonTest do not need to implement this.
     */
    @Test
    public void testCollectionMethodNamess() {
        // Arrange
        final List<String> names = getCollectionMethods()
                .stream()
                .map(Method::getName)
                .collect(Collectors.toList());

        // Assert
        assertThat(names)
                .containsExactly("bunchOfString");
    }

    /**
     * This test verifies the core BaseJacksonTest finds collection methods correctly.
     * Users of BaseJacksonTest do not need to implement this.
     */
    @Test
    public void testMapMethodNamess() {
        // Arrange
        final List<String> names = getMapMethods()
                .stream()
                .map(Method::getName)
                .collect(Collectors.toList());

        // Assert
        assertThat(names)
                .containsExactly("aMap");
    }

    /**
     * This test verifies the core BaseJacksonTest finds nullable methods correctly.
     * Users of BaseJacksonTest do not need to implement this.
     */
    @Test
    public void testNullableMethodNames() {
        // Arrange
        final List<String> names = getNullableMethods()
                .stream()
                .map(Method::getName)
                .collect(Collectors.toList());

        // Assert
        assertThat(names)
                .containsExactly("nullableString");
    }

    @Test
    public void testDefaultMethodNames() {
        final List<String> names = getDefaultMethods()
                .stream()
                .map(Method::getName)
                .collect(Collectors.toList());
        // Assert
        assertThat(names)
                .containsExactly("defaultString");
    }

    /**
     * This test verifies the core BaseJacksonTest finds optional methods correctly.
     * Users of BaseJacksonTest do not need to implement this.
     */
    @Test
    public void testOptionalMethodNames() {
        // Arrange
        final List<String> names = getOptionalMethods()
                .stream()
                .map(Method::getName)
                .collect(Collectors.toList());

        // Assert
        assertThat(names)
                .containsExactly("optionalString");
    }
}
