// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.translator;

import com.codeheadsystems.oop.mock.converter.JsonConverter;
import com.codeheadsystems.oop.mock.model.ImmutableMockedData;
import com.codeheadsystems.oop.mock.model.MockedData;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JsonTranslator implements Translator {

    private final JsonConverter converter;

    @Inject
    public JsonTranslator(final JsonConverter converter) {
        this.converter = converter;
    }

    @Override
    public <R> R unmarshal(final Class<R> clazz,
                           final MockedData marshalledData) {
        return converter.convert(marshalledData.marshalledData(), clazz);
    }

    @Override
    public <R> MockedData marshal(final R object) {
        return ImmutableMockedData.builder()
                .marshalledData(converter.toJson(object))
                .build();
    }
}
