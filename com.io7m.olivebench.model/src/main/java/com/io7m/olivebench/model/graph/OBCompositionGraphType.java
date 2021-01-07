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

import com.io7m.olivebench.exceptions.OBException;
import com.io7m.olivebench.model.names.OBName;

import java.util.Objects;
import java.util.UUID;

public interface OBCompositionGraphType extends OBCompositionGraphReadableType
{
  OBChannelType createChannel(
    UUID id,
    OBNodeMetadata nodeMetadata,
    OBChannelMetadata channelMetadata)
    throws OBException;

  OBChannelType createChannel(
    OBNodeMetadata nodeMetadata,
    OBChannelMetadata channelMetadata)
    throws OBException;

  default OBChannelType createChannel(
    final UUID id,
    final String name)
    throws OBException
  {
    return this.createChannel(
      id,
      OBNodeMetadata.builder()
        .setName(OBName.of(name))
        .build(),
      OBChannelMetadata.builder()
        .build()
    );
  }

  default OBChannelType createChannel(
    final String name)
    throws OBException
  {
    return this.createChannel(
      OBNodeMetadata.builder()
        .setName(OBName.of(name))
        .build(),
      OBChannelMetadata.builder()
        .build()
    );
  }

  void nodeDelete(
    OBCompositionNodeType node)
    throws OBException;

  boolean nodeIsDeleted(
    OBCompositionNodeType node);

  UUID createId();

  default <A, T extends OBRegionType<A>> T createRegion(
    final OBCompositionNodeType owner,
    final OBRegionConstructorType<A, T> constructor,
    final A regionData)
    throws OBException
  {
    Objects.requireNonNull(owner, "owner");
    Objects.requireNonNull(constructor, "constructor");
    Objects.requireNonNull(regionData, "regionData");

    return this.createRegion(
      owner,
      OBNodeMetadata.builder().build(),
      constructor,
      regionData
    );
  }

  default <A, T extends OBRegionType<A>> T createRegion(
    final OBCompositionNodeType owner,
    final UUID id,
    final OBRegionConstructorType<A, T> constructor,
    final A regionData)
    throws OBException
  {
    Objects.requireNonNull(owner, "owner");
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(constructor, "constructor");
    Objects.requireNonNull(regionData, "regionData");

    return this.createRegion(
      owner,
      id,
      OBNodeMetadata.builder().build(),
      constructor,
      regionData
    );
  }

  <A, T extends OBRegionType<A>> T createRegion(
    OBCompositionNodeType owner,
    OBNodeMetadata nodeMetadata,
    OBRegionConstructorType<A, T> constructor,
    A regionData)
    throws OBException;

  <A, T extends OBRegionType<A>> T createRegion(
    OBCompositionNodeType owner,
    UUID id,
    OBNodeMetadata nodeMetadata,
    OBRegionConstructorType<A, T> constructor,
    A regionData)
    throws OBException;
}
