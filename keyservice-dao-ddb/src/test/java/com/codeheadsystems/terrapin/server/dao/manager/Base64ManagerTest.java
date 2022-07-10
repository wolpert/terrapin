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

package com.codeheadsystems.terrapin.server.dao.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.junit.jupiter.api.Test;

class Base64ManagerTest {

    @Test
    public void testRoundTrip() {
        final Random random = new Random();
        byte[] src = new byte[20];
        random.nextBytes(src);
        final Base64Manager manager = new Base64Manager();
        final String encoded = manager.to(src);
        final byte[]dest = manager.from(encoded);
        assertThat(dest)
                .isEqualTo(src);

    }

}