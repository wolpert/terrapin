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

package com.codeheadsystems.oop.mock.translator;

import com.codeheadsystems.oop.mock.model.MockedData;

/**
 * Provides the mechanism to un/marshal results. This can be used to store values in the datastore,
 * as well as get them out.
 *
 * The test client that stores the value must use the same translator as the server.
 */
public interface Translator {

    /**
     * Convert the marshalled text back to the original object.
     *
     * @param clazz class of what we are returning.
     * @param marshalledData that we converted before.
     * @param <R> type of object.
     * @return the object.
     */
    <R> R unmarshal(Class<R> clazz, MockedData marshalledData);

    /**
     * Convert the object to text that can be stored.
     *
     * @param object to marshall.
     * @param <R> type of object.
     * @return the marshall text.
     */
    <R> MockedData marshal(R object);
}
