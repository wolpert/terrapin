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

import com.codeheadsystems.terrapin.keystore.model.RNG;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import java.security.SecureRandom;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Contains the random provider we want to use.
 */
@Module(includes = {RNGModule.Binder.class})
public class RNGModule {

    public static final String RNG = "RNG";

    @Provides
    @Singleton
    @Named(RNG)
    public RNG rng(@Named(Binder.RNG_IMPL) final Optional<RNG> rng) {
        return rng.orElseGet(this::defaultRNG);
    }

    public RNG defaultRNG() {
        final SecureRandom random = new SecureRandom();
        random.setSeed(System.currentTimeMillis());
        return random::nextBytes;
    }

    @Module
    public interface Binder {
        String RNG_IMPL = "RNG_IMPL";

        @BindsOptionalOf
        @Named(RNG_IMPL)
        RNG rng();
    }

}
