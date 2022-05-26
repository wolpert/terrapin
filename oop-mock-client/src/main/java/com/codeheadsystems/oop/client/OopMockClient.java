// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.client;

public interface OopMockClient {

    <R> void mockSetup(R mockData, String lookup, String id);

    void deleteMock(String lookup, String id);

}
