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

package com.codeheadsystems.keystore.api;

import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Manages the keys available for the service.
 */
@Path("/v1/key")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface KeyManagerService {

  /**
   * Creates a new key with the id given. Note that the resulting key identifier includes the owner of the key.
   *
   * @param owner the owner
   * @param keyId to create for this owner.
   * @return key. key
   */
  @PUT
  @Timed
  @Path("/{owner}/{id}")
  Key create(@PathParam("owner") String owner, @PathParam("id") String keyId);

  /**
   * Deletes all the versions of this key. No version of the key can be used after this.
   *
   * @param owner the owner
   * @param keyId to be deleted.
   * @return the response
   */
  @DELETE
  @Timed
  @Path("/{owner}/{id}")
  Response delete(@PathParam("owner") String owner, @PathParam("id") String keyId);

  /**
   * Deletes a specific versions of this key. This version of the key can no longer be used after this.
   *
   * @param owner   the owner
   * @param keyId   to be deleted.
   * @param version to be deleted.
   * @return the response
   */
  @DELETE
  @Timed
  @Path("/{owner}/{id}/{version}")
  Response delete(@PathParam("owner") String owner, @PathParam("id") String keyId, @PathParam("version") Long version);

}
