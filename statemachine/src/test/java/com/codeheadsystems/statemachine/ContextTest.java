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

package com.codeheadsystems.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.statemachine.converter.InvocationModelConverter;
import com.codeheadsystems.statemachine.exceptions.TargetException;
import com.codeheadsystems.statemachine.manager.InvocationManager;
import com.codeheadsystems.statemachine.manager.StateMachineManager;
import com.codeheadsystems.statemachine.manager.TransitionManager;
import com.codeheadsystems.statemachine.model.ActiveStateMachine;
import com.codeheadsystems.statemachine.model.InvocationModel;
import com.codeheadsystems.statemachine.model.StateMachine;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContextTest extends BaseMetricTest {
    private static final String IDENTIFIER = "identifier";
    private static final String TRANSITION = "transition";

    @Mock private StateMachineManager stateMachineManager;
    @Mock private TransitionManager transitionManager;
    @Mock private InvocationManager invocationManager;
    @Mock private InvocationModelConverter invocationModelConverter;
    @Mock private InvocationModel<Object> invocationModel;
    @Mock private StateMachine stateMachine;
    @Mock private Set<String> stringSet;
    @Captor private ArgumentCaptor<ActiveStateMachine<Object>> activeStateMachineArgumentCaptor;

    private Context context;

    @BeforeEach
    void setUp() {
        context = new Context(stateMachineManager, transitionManager, invocationModelConverter, invocationManager, metricManager);
    }

    @Test
    void register_loop() {
        setupRegistration();

        assertThat(context.isRegistered(Object.class))
            .isTrue();
        assertThat(context.getRegisteredClasses())
            .isNotNull()
            .hasSize(1)
            .contains(Object.class);
        assertThat(context.getStateMachineForClass(Object.class))
            .isPresent()
            .contains(stateMachine);

        context.deregister(Object.class);
        assertThat(context.isRegistered(Object.class))
            .isFalse();

        context.deregister(Object.class);
        assertThat(context.isRegistered(Object.class))
            .isFalse();
    }

    @Test
    void transition() {
        setupRegistration();
        final Object object = new Object();

        context.transition(object, TRANSITION);

        verify(transitionManager).transition(activeStateMachineArgumentCaptor.capture(), eq(object), eq(TRANSITION));
        assertThat(activeStateMachineArgumentCaptor.getValue())
            .hasFieldOrPropertyWithValue("stateMachine", stateMachine)
            .hasFieldOrPropertyWithValue("invocationModel", invocationModel);
    }

    @Test
    void transition_noneFound() {
        setupRegistration();
        final Object object = "blah";

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> context.transition(object, TRANSITION));
    }

    @Test
    void transitions() {
        setupRegistration();
        final Object object = new Object();
        when(transitionManager.transitions(activeStateMachineArgumentCaptor.capture(), eq(object)))
            .thenReturn(stringSet);

        final Set<String> result = context.transitions(object);

        assertThat(result).isEqualTo(stringSet);
        assertThat(activeStateMachineArgumentCaptor.getValue())
            .hasFieldOrPropertyWithValue("stateMachine", stateMachine)
            .hasFieldOrPropertyWithValue("invocationModel", invocationModel);
    }

    @Test
    void transitions_noneFound() {
        setupRegistration();
        final Object object = "blah";

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> context.transitions(object));
    }

    @Test
    void register_targetClass_notFound() {
        when(stateMachineManager.generateFromAnnotation(Object.class)).thenReturn(Optional.empty());

        assertThatExceptionOfType(TargetException.class)
            .isThrownBy(() -> context.register(Object.class));
    }

    @Test
    void register_targetClass_found() {
        when(stateMachineManager.generateFromAnnotation(Object.class)).thenReturn(Optional.of(stateMachine));
        when(invocationModelConverter.generate(Object.class)).thenReturn(invocationModel);
        when(invocationModel.targetClass()).thenReturn(Object.class);
        when(stateMachine.identifier()).thenReturn(IDENTIFIER);

        context.register(Object.class);
        assertThat(context.isRegistered(Object.class))
            .isTrue();
    }

    private void setupRegistration() {
        when(invocationModelConverter.generate(Object.class)).thenReturn(invocationModel);
        when(invocationModel.targetClass()).thenReturn(Object.class);
        when(stateMachine.identifier()).thenReturn(IDENTIFIER);

        context.register(Object.class, stateMachine);
    }
}