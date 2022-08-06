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

package com.codeheadsystems.terrapin.keystore.module;

import com.codeheadsystems.terrapin.keystore.resource.JettyResource;
import com.codeheadsystems.terrapin.keystore.resource.KeyManagerResource;
import com.codeheadsystems.terrapin.keystore.resource.KeyReaderResource;
import com.codeheadsystems.terrapin.keystore.resource.KeyRotationResource;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface ResourceModule {

  @Binds
  @IntoSet
  JettyResource keyManagerResource(KeyManagerResource resource);

  @Binds
  @IntoSet
  JettyResource keyReaderResource(KeyReaderResource resource);

  @Binds
  @IntoSet
  JettyResource keyRotationResource(KeyRotationResource resource);
}
