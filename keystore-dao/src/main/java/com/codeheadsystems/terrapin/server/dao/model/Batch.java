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

package com.codeheadsystems.terrapin.server.dao.model;

import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface Batch<T> {

    List<T> list();

    /**
     * If empty, there is no more keys. WARNING: This will potentially contain result data. Treat it as you would table
     * results.
     *
     * @return optional token for the next request.
     */
    Optional<Token> nextToken();

}
