// Copyright (c) 2022. CodeHead Systems. All rights reserved
// Ned Wolpert <ned.wolpert@codeheadsystems.com>

package com.codeheadsystems.metrics;

/**
 * This needs to return the base metrics implementation. The part that sends the metrics to the backend.
 */
public interface MetricsImplementationFactory {

    MetricsImplementation get();

}
