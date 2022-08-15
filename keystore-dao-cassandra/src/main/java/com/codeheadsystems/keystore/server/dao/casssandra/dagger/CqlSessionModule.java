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

package com.codeheadsystems.keystore.server.dao.casssandra.dagger;

import com.codeheadsystems.keystore.server.dao.casssandra.configuration.TableConfiguration;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.type.codec.ExtraTypeCodecs;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import java.net.InetSocketAddress;
import java.util.Set;
import javax.inject.Singleton;

/**
 * Provides type codes we need for cassandra.
 */
@Module
public class CqlSessionModule {

  public static final String DATACENTER = "datacenter1";

  private final String localDataCenter;
  private final InetSocketAddress[] addresses;

  public CqlSessionModule(final InetSocketAddress... addresses) {
    this(DATACENTER, addresses);
  }

  public CqlSessionModule(final String localDataCenter,
                          final InetSocketAddress... addresses) {
    this.localDataCenter = localDataCenter;
    this.addresses = addresses;
  }

  @Provides
  @Singleton
  @IntoSet
  public TypeCodec<?> zonedTimeStamp() {
    return TypeCodecs.ZONED_TIMESTAMP_UTC;
  }

  @Provides
  @Singleton
  @IntoSet
  public TypeCodec<?> blobToArray() {
    return ExtraTypeCodecs.BLOB_TO_ARRAY;
  }

  /**
   * The CQL session builder.
   *
   * @param codecs codecs for conversion.
   * @param tableConfiguration cassandra configuration.
   * @return an instance.
   */
  @Provides
  @Singleton
  public CqlSession cqlSession(final Set<TypeCodec<?>> codecs,
                               final TableConfiguration tableConfiguration) {
    TypeCodec<?>[] codecsArray = new TypeCodec[codecs.size()];
    codecsArray = codecs.toArray(codecsArray);
    final CqlSessionBuilder builder = CqlSession.builder()
        .addTypeCodecs(codecsArray)
        .withKeyspace(tableConfiguration.keyspace())
        .withLocalDatacenter(localDataCenter);
    for (InetSocketAddress address : addresses) {
      builder.addContactPoint(address);
    }
    return builder.build();
  }

}
