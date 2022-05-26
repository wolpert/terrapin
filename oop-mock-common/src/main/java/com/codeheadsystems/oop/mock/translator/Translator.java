// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

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
