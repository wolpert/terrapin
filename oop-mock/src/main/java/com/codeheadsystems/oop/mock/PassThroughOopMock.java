// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock;

import com.codeheadsystems.oop.OopMock;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Pass through oop mock. The default response. This is forced into a singleton since it's likely
 * highly shared.
 */
@Singleton
public class PassThroughOopMock implements OopMock {

    @Inject
    public PassThroughOopMock() {

    }

    @Override
    public <R> R proxy(Class<R> returnClass, Supplier<R> supplier, String lookup, String id) {
        return supplier.get();
    }

    @Override
    public String toString() {
        return "PassThroughOopMock{} (OopMock disabled)";
    }
}
