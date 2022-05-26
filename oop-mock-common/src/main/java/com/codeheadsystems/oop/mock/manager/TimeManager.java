// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.oop.mock.manager;

import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TimeManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeManager.class);

    @Inject
    public TimeManager() {
        LOGGER.info("TimeManager()");
    }

    public <R> R logTimed(final Supplier<R> supplier) {
        final long start = System.currentTimeMillis();
        try {
            return supplier.get();
        } finally {
            final long end = System.currentTimeMillis();
            LOGGER.info("Execution MS: {}", end - start);
        }

    }


}