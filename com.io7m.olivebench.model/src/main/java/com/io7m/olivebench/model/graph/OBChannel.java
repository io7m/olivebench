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

import com.io7m.olivebench.model.properties.OBProperty;
import com.io7m.olivebench.model.properties.OBPropertyType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.strings.OBStringsType;

import java.util.Objects;
import java.util.UUID;

public final class OBChannel extends OBAbstractNode implements OBChannelType
{
  private final OBPropertyType<OBChannelMetadata> channelMetadata;

  private OBChannel(
    final OBCompositionGraphType inGraph,
    final OBStringsType inStrings,
    final UUID inId,
    final OBNodeMetadata nodeMetadata,
    final OBChannelMetadata channelMetadata)
  {
    super(
      inGraph,
      inStrings,
      inId,
      nodeMetadata
    );

    this.channelMetadata =
      OBProperty.create(
        Objects.requireNonNull(channelMetadata, "channelMetadata"));
  }

  public static OBChannelType create(
    final OBServiceDirectoryType services,
    final OBCompositionGraphType graph,
    final UUID id,
    final OBNodeMetadata nodeMetadata,
    final OBChannelMetadata channelMetadata)
  {
    return new OBChannel(
      graph,
      services.requireService(OBStringsType.class),
      id,
      nodeMetadata,
      channelMetadata);
  }

  @Override
  public String type()
  {
    return this.strings().channel();
  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBChannel %s '%s']",
      this.id(),
      super.nodeMetadata().read().name().value());
  }

  @Override
  public OBPropertyType<OBChannelMetadata> channelMetadata()
  {
    return this.channelMetadata;
  }
}
