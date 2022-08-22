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

package com.codeheadsystems.keystore.integ.client;

import com.codeheadsystems.keystore.api.KeyReaderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;
import feign.micrometer.MicrometerCapability;
import feign.slf4j.Slf4jLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mechanism to get reader services from the client.
 */
public class ClientFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);
  private final String connectionUrl;
  private final Feign.Builder builder;

  /**
   * Default constructor.
   *
   * @param connectionUrl to connect with.
   * @param objectMapper for converting to objects.
   */
  public ClientFactory(final String connectionUrl,
                       final ObjectMapper objectMapper) {
    LOGGER.info("ClientFactory({},{})", connectionUrl, objectMapper);
    this.connectionUrl = connectionUrl;
    this.builder = Feign.builder()
        .logger(new Slf4jLogger())
        .contract(new JAXRSContract())
        .addCapability(new MicrometerCapability())
        .decoder(new JacksonDecoder(objectMapper))
        .encoder(new JacksonEncoder(objectMapper));
  }

  public KeyReaderService keyReaderService() {
    LOGGER.debug("keyReaderService()");
    return builder.target(KeyReaderService.class, connectionUrl);
  }

}
