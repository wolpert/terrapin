// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.manager;

import com.codeheadsystems.oop.mock.model.MockedData;
import com.codeheadsystems.oop.mock.resolver.MockDataResolver;
import com.codeheadsystems.oop.mock.translator.Translator;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ProxyManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyManager.class);
    private final MockDataResolver resolver;
    private final Translator translator;

    @Inject
    public ProxyManager(final MockDataResolver resolver,
                        final Translator translator) {
        this.resolver = resolver;
        this.translator = translator;
    }

    public <R> R proxy(final String namespace,
                       final String lookup,
                       final String id,
                       final Class<R> returnClass,
                       final Supplier<R> supplier) {
        LOGGER.debug("proxy({},{}, {})", namespace, lookup, id);
        final Optional<MockedData> mockedData = resolver.resolve(namespace, lookup, id);
        if (mockedData.isPresent()) {
            final MockedData unmarshalled = mockedData.get();
            LOGGER.info("Found mocked result: {},{} -> {}", lookup, id, unmarshalled);
            return translator.unmarshal(returnClass, unmarshalled);
        } else {
            LOGGER.debug("Not mocked: {},{}", lookup, id);
            return supplier.get();
        }
    }
}
