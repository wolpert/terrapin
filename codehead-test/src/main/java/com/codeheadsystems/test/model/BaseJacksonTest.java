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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.assertj.core.api.Condition;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(MockitoExtension.class)
public abstract class BaseJacksonTest<T> {

    protected static Set<String> methodsToIgnore;

    private static final Condition<Optional<?>> PRESENT = new Condition<>(Optional::isPresent, "isPresent");

    protected ObjectMapper objectMapper;
    protected String simpleName;

    /**
     * Define the model interface/class that we are testing. We use this query for properties we want.
     *
     * @return Class that we are testing.
     */
    protected abstract Class<T> getBaseClass();

    /**
     * Return a standard instance of the class that should go back and forth to json. Note, we will use the
     * equals method of this class to validate.
     *
     * @return an instance of the class.
     */
    protected abstract T getInstance();

    /**
     * Override this if you have a custom object mapper to use. This will be retrieved once per test.
     *
     * @return object mapper.
     */
    protected ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new Jdk8Module());
    }

    @BeforeAll
    public static void findObjectMethods() {
        methodsToIgnore = Arrays.stream(Object.class.getDeclaredMethods()).map(Method::getName).collect(Collectors.toSet());
    }

    @BeforeEach
    public void baseJacksonTestSetup() {
        objectMapper = objectMapper();
        simpleName = getBaseClass().getSimpleName();
    }

    /**
     * Test verifies that we can write json from the model, and convert that json back to the object.
     *
     * @throws JsonProcessingException if object mapper fails.
     */
    @Test
    public void testRoundTrip() throws JsonProcessingException {
        // Arrange
        final T instance = getInstance();
        final String json = objectMapper.writeValueAsString(getInstance());

        // Act
        final T unwoundInstance = objectMapper.readValue(json, getBaseClass());

        // Assert
        assertThat(unwoundInstance)
            .describedAs("Verification %s can go to json and back to an object", simpleName)
            .isEqualTo(instance);
    }

    /**
     * Test verifies that we can write json from the model, and convert that json back to the object, even if
     * the json has extra fields added.
     *
     * @throws JsonProcessingException if object mapper fails.
     */
    @Test
    public void testRoundTripWithExtraCharacters() throws JsonProcessingException {
        // Arrange
        final T instance = getInstance();
        final String json = objectMapper.writeValueAsString(getInstance());
        final ObjectNode objectNode = objectMapper.readValue(json, ObjectNode.class);
        objectNode.put("someWierdFieldWeDontCareAbout", "whatevervalue");
        final String jsonWithExtraStuff = objectMapper.writeValueAsString(objectNode);

        // Act
        final T unwoundInstance = objectMapper.readValue(jsonWithExtraStuff, getBaseClass());

        // Assert
        assertThat(unwoundInstance)
            .describedAs("Verification %s can go to json and back to an object with extra fields", simpleName)
            .isEqualTo(instance);
    }

    /**
     * Test verifies expected methods that are required by the model will fail the JSON conversion if they are missing.
     *
     * @throws JsonProcessingException if object mapper fails.
     */
    @Test
    public void testNotNullMethods() throws JsonProcessingException {
        // Arrange
        final String json = objectMapper.writeValueAsString(getInstance());
        for (Method method : getRequiredMethods()) {
            final ObjectNode objectNode = objectMapper.readValue(json, ObjectNode.class);
            final String methodName = method.getName();
            objectNode.remove(methodName);
            final String reducedJson = objectMapper.writeValueAsString(objectNode);

            // Act
            final Throwable thrown = catchThrowable(() -> objectMapper.readValue(reducedJson, getBaseClass()));

            // Assert
            assertThat(thrown)
                .describedAs("Throw test fail %s.%s()", simpleName, methodName)
                .isNotNull()
                .isInstanceOf(ValueInstantiationException.class)
                .hasMessageContaining("Cannot construct instance of");
        }
    }

    /**
     * Test verifies expected methods that are not required by the model will not fail the JSON conversion if they are missing.
     *
     * @throws JsonProcessingException if object mapper fails.
     */
    @Test
    public void testNullableMethods() throws JsonProcessingException, InvocationTargetException, IllegalAccessException {
        // Arrange
        final T instance = getInstance();
        final String json = objectMapper.writeValueAsString(getInstance());

        // Act
        for (Method method : getNullableMethods()) {
            final ObjectNode objectNode = objectMapper.readValue(json, ObjectNode.class);
            final String methodName = method.getName();
            objectNode.remove(methodName);
            final String reducedJson = objectMapper.writeValueAsString(objectNode);
            final T reducedInstance = objectMapper.readValue(reducedJson, getBaseClass());

            // Assert
            assertThat(instance)
                .describedAs("Expected object equality to fail when removing %s.%s")
                .isNotEqualTo(reducedInstance);
            assertThat(method.invoke(instance))
                .describedAs("Setup fail %s.%s()", simpleName, methodName)
                .isNotNull();
            assertThat(method.invoke(reducedInstance))
                .describedAs("Method fail %s.%s()", simpleName, methodName)
                .isNull();
        }
    }

    /**
     * Test verifies expected methods that are lists,sets,etc will not fail the JSON conversion if they are missing.
     * Note, this does not handle min quantites.
     *
     * @throws JsonProcessingException if object mapper fails.
     */
    @Test
    public void testCollectionMethods() throws JsonProcessingException, InvocationTargetException, IllegalAccessException {
        // Arrange
        final T instance = getInstance();
        final String json = objectMapper.writeValueAsString(instance);

        // Act
        for (Method method : getCollectionMethods()) {
            final ObjectNode objectNode = objectMapper.readValue(json, ObjectNode.class);
            final String methodName = method.getName();
            objectNode.remove(methodName);
            final String reducedJson = objectMapper.writeValueAsString(objectNode);
            final T reducedInstance = objectMapper.readValue(reducedJson, getBaseClass());

            // Assert
            assertThat(instance).isNotEqualTo(reducedInstance);
            assertThat(method.invoke(instance))
                .describedAs("Setup fail %s.%s()", simpleName, methodName)
                .isNotNull()
                .asInstanceOf(InstanceOfAssertFactories.ITERABLE)
                .isNotEmpty();
            assertThat(method.invoke(reducedInstance))
                .describedAs("Method fail %s.%s()", simpleName, methodName)
                .isNotNull()
                .asInstanceOf(InstanceOfAssertFactories.ITERABLE)
                .isEmpty();
        }
    }

    /**
     * Test verifies expected methods that are maps will not fail the JSON conversion if they are missing.
     *
     * @throws JsonProcessingException if object mapper fails.
     */
    @Test
    public void testMapMethods() throws JsonProcessingException, InvocationTargetException, IllegalAccessException {
        // Arrange
        final T instance = getInstance();
        final String json = objectMapper.writeValueAsString(instance);

        // Act
        for (Method method : getMapMethods()) {
            final ObjectNode objectNode = objectMapper.readValue(json, ObjectNode.class);
            final String methodName = method.getName();
            objectNode.remove(methodName);
            final String reducedJson = objectMapper.writeValueAsString(objectNode);
            final T reducedInstance = objectMapper.readValue(reducedJson, getBaseClass());

            // Assert
            assertThat(instance)
                .isNotEqualTo(reducedInstance);
            assertThat(method.invoke(instance))
                .describedAs("Setup fail %s.%s()", simpleName, methodName)
                .isNotNull()
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .isNotEmpty();
            assertThat(method.invoke(reducedInstance))
                .describedAs("Method fail %s.%s()", simpleName, methodName)
                .isNotNull()
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .isEmpty();
        }
    }

    /**
     * Test verifies expected methods that are optional by the model will not fail the JSON conversion if they are missing.
     *
     * @throws JsonProcessingException if object mapper fails.
     */
    @Test
    public void testOptionalMethods() throws JsonProcessingException, InvocationTargetException, IllegalAccessException {
        // Arrange
        final T instance = getInstance();
        final String json = objectMapper.writeValueAsString(instance);

        // Act
        for (Method method : getOptionalMethods()) {
            final ObjectNode objectNode = objectMapper.readValue(json, ObjectNode.class);
            final String methodName = method.getName();
            objectNode.remove(methodName);
            final String reducedJson = objectMapper.writeValueAsString(objectNode);
            final T reducedInstance = objectMapper.readValue(reducedJson, getBaseClass());

            // Assert
            assertThat(instance).isNotEqualTo(reducedInstance);
            final Object instanceValue = method.invoke(instance);
            final Object reducedValue = method.invoke(reducedInstance);
            assertThat(instanceValue)
                .describedAs("Setup fail %s.%s()", simpleName, methodName)
                .isNotNull()
                .isInstanceOf(Optional.class)
                .asInstanceOf(InstanceOfAssertFactories.OPTIONAL)
                .is(PRESENT);
            assertThat(reducedValue)
                .describedAs("Setup fail %s.%s()", simpleName, methodName)
                .isNotNull()
                .isInstanceOf(Optional.class)
                .asInstanceOf(InstanceOfAssertFactories.OPTIONAL)
                .isNot(PRESENT);
        }
    }

    List<Method> getRequiredMethods() {
        final Class<T> clazz = getBaseClass();
        return Arrays.stream(clazz.getMethods())
            .filter(m ->!methodsToIgnore.contains(m.getName()))
            .filter(m -> m.getDeclaredAnnotation(Nullable.class) == null)
            .filter(m -> m.getDeclaredAnnotation(JsonIgnore.class) == null)
            .filter(m -> !m.getReturnType().equals(Optional.class))
            .filter(m -> !Collection.class.isAssignableFrom(m.getReturnType()))
            .filter(m -> !Map.class.isAssignableFrom(m.getReturnType()))
            .collect(Collectors.toList());
    }

    /**
     * Provide a list of methods that results in collection objects.
     *
     * @return list of methods.
     */
    List<Method> getCollectionMethods() {
        final Class<T> clazz = getBaseClass();
        return Arrays.stream(clazz.getMethods())
            .filter(m ->!methodsToIgnore.contains(m.getName()))
            .filter(m -> m.getDeclaredAnnotation(JsonIgnore.class) == null)
            .filter(m -> Collection.class.isAssignableFrom(m.getReturnType()))
            .collect(Collectors.toList());
    }

    /**
     * Provide a list of methods that results in map objects.
     *
     * @return list of methods.
     */
    List<Method> getMapMethods() {
        final Class<T> clazz = getBaseClass();
        return Arrays.stream(clazz.getMethods())
            .filter(m ->!methodsToIgnore.contains(m.getName()))
            .filter(m -> m.getDeclaredAnnotation(JsonIgnore.class) == null)
            .filter(m -> Map.class.isAssignableFrom(m.getReturnType()))
            .collect(Collectors.toList());
    }

    /**
     * Provide a list of methods that results in nullable objects.
     *
     * @return list of methods.
     */
    List<Method> getNullableMethods() {
        final Class<T> clazz = getBaseClass();
        return Arrays.stream(clazz.getMethods())
            .filter(m ->!methodsToIgnore.contains(m.getName()))
            .filter(m -> m.getDeclaredAnnotation(JsonIgnore.class) == null)
            .filter(m -> m.getDeclaredAnnotation(Nullable.class) != null)
            .collect(Collectors.toList());
    }

    /**
     * Provide a list of methods that results in optional objects.
     *
     * @return list of methods.
     */
    List<Method> getOptionalMethods() {
        final Class<T> clazz = getBaseClass();
        return Arrays.stream(clazz.getMethods())
            .filter(m ->!methodsToIgnore.contains(m.getName()))
            .filter(m -> m.getDeclaredAnnotation(JsonIgnore.class) == null)
            .filter(m -> m.getReturnType().equals(Optional.class))
            .collect(Collectors.toList());
    }

}
