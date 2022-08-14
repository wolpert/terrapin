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

package com.codeheadsystems.keystore.client;

import com.codeheadsystems.keystore.api.Key;
import com.codeheadsystems.keystore.api.KeyReaderService;
import io.micrometer.core.instrument.MeterRegistry;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides for a key reader.
 */
@Singleton
public class KeyReaderClient implements KeyReaderService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyReaderClient.class);

  private final MeterRegistry meterRegistry;
  private final ClientConfiguration configuration;

  /**
   * Default constructor.
   *
   * @param meterRegistry used for metrics.
   * @param configuration how we contact the client.
   */
  @Inject
  public KeyReaderClient(final MeterRegistry meterRegistry,
                         final ClientConfiguration configuration) {
    LOGGER.info("KeyReaderClient({},{})", configuration, meterRegistry);
    this.meterRegistry = meterRegistry;
    this.configuration = configuration;
  }

  @Override
  public Key get(final String owner, final String keyId) {
    return null;
  }

  @Override
  public Key get(final String owner, final String keyId, final Long version) {
    return null;
  }
}
