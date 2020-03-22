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

package com.io7m.olivebench.model.graph;

import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.exceptions.OBException;
import com.io7m.olivebench.model.names.OBName;
import com.io7m.olivebench.model.spaces.OBSpaceRegionType;

import java.util.UUID;

public interface OBCompositionGraphType extends OBCompositionGraphReadableType
{
  OBChannelType createChannel(
    UUID id,
    OBName name)
    throws OBException;

  OBChannelType createChannel(
    OBName name)
    throws OBException;

  default OBChannelType createChannel(
    final UUID id,
    final String name)
    throws OBException
  {
    return this.createChannel(id, OBName.of(name));
  }

  default OBChannelType createChannel(
    final String name)
    throws OBException
  {
    return this.createChannel(OBName.of(name));
  }

  void nodeDelete(
    OBCompositionNodeType node)
    throws OBException;

  boolean nodeIsDeleted(
    OBCompositionNodeType node);

  UUID createId();

  <T extends OBRegionType> T createRegion(
    OBCompositionNodeType owner,
    PAreaL<OBSpaceRegionType> area,
    OBRegionConstructorType<T> constructor);

  <T extends OBRegionType> T createRegion(
    OBCompositionNodeType owner,
    UUID id,
    PAreaL<OBSpaceRegionType> area,
    OBRegionConstructorType<T> constructor)
    throws OBException;
}
