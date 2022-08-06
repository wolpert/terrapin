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

package com.codeheadsystems.terrapin.common.crypt;

import com.codeheadsystems.terrapin.common.manager.ConstructorManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bouncycastle.crypto.modes.AEADCipher;

@Singleton
public class CryptoProvider<T extends AEADCipher> {

    private final LoadingCache<Class<T>, AEADCipherCryptor<T>> cache;
    private final ConstructorManager constructorManager;

    @Inject
    public CryptoProvider(final ConstructorManager constructorManager) {
        this.constructorManager = constructorManager;
        this.cache = CacheBuilder.newBuilder().build(CacheLoader.from(this::from));
    }

    private <T extends AEADCipher> AEADCipherCryptor<T> from(final Class<T> clazz) {
        final Supplier<T> supplier = constructorManager.defaultConstructor(clazz);
        return new AEADCipherCryptor<>(supplier);
    }

    public Cryptor cryptor(final CryptorType type) {
        return cache.getUnchecked((Class<T>) type.getClazz());
    }

}
