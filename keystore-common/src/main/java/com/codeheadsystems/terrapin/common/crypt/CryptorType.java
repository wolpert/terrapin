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

import java.util.function.Supplier;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.AEADCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.modes.GCMSIVBlockCipher;

public enum CryptorType {
    AES_256_GCM_SIV(GCMSIVBlockCipher::new, 32, 12),
    AES_128_GCM_SIV(GCMSIVBlockCipher::new, 16, 12),
    AES_256_GCM(() -> new GCMBlockCipher(new AESEngine()), 32, 12),
    AES_128_GCM(() -> new GCMBlockCipher(new AESEngine()), 16, 12);

    private final Supplier<? extends AEADCipher> supplier;
    private final int ivLength;
    private final int keyLength;

    CryptorType(final Supplier<? extends AEADCipher> supplier,
                final int keyLength,
                final int ivLength) {
        this.supplier = supplier;
        this.keyLength = keyLength;
        this.ivLength = ivLength;
    }

    public Supplier<? extends AEADCipher> getSupplier() {
        return supplier;
    }

    public int getIvLength() {
        return ivLength;
    }

    public int getKeyLength() {
        return keyLength;
    }
}
