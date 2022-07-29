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

package com.codeheadsystems.terrapin.keystore.api;

import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Manages the keys available for the service.
 */
@Path("/key")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface KeyService {

  /**
   * Creates a new key with the id given. Note that the resulting key identifier includes the owner of the key.
   *
   * @param keyId to create for this owner.
   * @return key.
   */
  @PUT
  @Timed
  @Path("/")
  Key create(@QueryParam("id") String keyId);

  /**
   * Gets the latest version of this key.
   *
   * @param keyId to be found.
   * @return Key.
   */
  @GET
  @Timed
  @Path("/{id}")
  Key get(@PathParam("id") String keyId);

  /**
   * Gets a specific version of this key.
   *
   * @param keyId   to be found.
   * @param version to be found.
   * @return Key.
   */
  @GET
  @Timed
  @Path("/{id}/version/{version}")
  Key get(@PathParam("id") String keyId, @PathParam("version") Integer version);


  /**
   * Rotates the current key. New version, old version will expire out.
   *
   * @param keyId that needs rotating.
   * @return a new key based on the old one.
   */
  @POST
  @Timed
  @Path("/{id}/rotate")
  Key rotate(@PathParam("id") String keyId);

  /**
   * Deletes all the versions of this key. No version of the key can be used after this.
   *
   * @param keyId to be deleted.
   */
  @DELETE
  @Timed
  @Path("/{id}")
  Response delete(@PathParam("id") String keyId);

  /**
   * Deletes a specific versions of this key. This version of the key can no longer be used after this.
   *
   * @param keyId   to be deleted.
   * @param version to be deleted.
   */
  @DELETE
  @Timed
  @Path("/{id}/version/{version}")
  Response delete(@PathParam("id") String keyId, @PathParam("version") Integer version);

}
