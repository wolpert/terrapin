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

package com.codeheadsystems.oop.mock.manager;

import java.io.InputStream;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class ResourceLookupManager {

  public static final String LOOKUP_CLASS = "ResourceLookupManager.lookupClass";
  private final ClassLoader lookupClassLoader;

  @Inject
  public ResourceLookupManager(@Named(LOOKUP_CLASS) final Optional<ClassLoader> lookupClassLoader) {
    this.lookupClassLoader = lookupClassLoader.orElse(ResourceLookupManager.class.getClassLoader());
  }

  public Optional<InputStream> inputStream(final String filename) {
    return Optional.ofNullable(lookupClassLoader.getResourceAsStream(filename));
  }

}
