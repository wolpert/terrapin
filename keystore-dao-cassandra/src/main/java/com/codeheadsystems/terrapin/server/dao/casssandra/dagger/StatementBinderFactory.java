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

package com.codeheadsystems.terrapin.server.dao.casssandra.dagger;

import com.codeheadsystems.terrapin.server.dao.casssandra.manager.StatementBinder;
import dagger.assisted.AssistedFactory;
import java.util.function.Function;

/**
 * Factory from Dagger to create PreparedStatementManagers.
 */
@AssistedFactory
public interface StatementBinderFactory {

  StatementBinder build(final String cqlStatement,
                        final Function<Object, Object[]> binder);

}
