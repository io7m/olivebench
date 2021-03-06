/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.olivebench.services.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class OBServiceDirectory implements OBServiceDirectoryType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBServiceDirectory.class);

  private final ConcurrentHashMap<Object, Object> services;

  public OBServiceDirectory()
  {
    this.services = new ConcurrentHashMap<>();
  }

  public <T extends OBServiceType> void register(
    final Class<T> clazz,
    final T service)
  {
    LOG.debug("register: {} → {}", clazz, service);
    this.services.put(clazz, service);
  }

  @Override
  public <T extends OBServiceType> Optional<T> optionalService(
    final Class<T> clazz)
  {
    Objects.requireNonNull(clazz, "clazz");
    return Optional.ofNullable(this.services.get(clazz)).map(clazz::cast);
  }

  @Override
  public <T extends OBServiceType> T requireService(
    final Class<T> clazz)
    throws OBServiceException
  {
    return this.optionalService(clazz)
      .orElseThrow(() -> new OBServiceException(
        String.format(
          "No implementations available of type %s",
          clazz.getCanonicalName())
      ));
  }
}
