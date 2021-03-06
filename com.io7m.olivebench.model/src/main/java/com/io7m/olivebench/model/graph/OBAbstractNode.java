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
import com.io7m.olivebench.model.names.OBName;
import com.io7m.olivebench.model.properties.OBProperty;
import com.io7m.olivebench.model.properties.OBPropertyType;
import com.io7m.olivebench.strings.OBStringsType;

import java.util.Objects;
import java.util.UUID;

public abstract class OBAbstractNode implements OBCompositionNodeType
{
  private final OBCompositionGraphType graph;
  private final OBStringsType strings;
  private final UUID id;
  private final OBPropertyType<OBNodeMetadata> nodeMetadata;

  protected OBAbstractNode(
    final OBCompositionGraphType inGraph,
    final OBStringsType inStrings,
    final UUID inId)
  {
    this(
      inGraph,
      inStrings,
      inId,
      OBNodeMetadata.builder()
        .setArea(PAreaL.of(0L, 0L, 0L, 0L))
        .setName(OBName.of(""))
        .build()
    );
  }

  protected OBAbstractNode(
    final OBCompositionGraphType inGraph,
    final OBStringsType inStrings,
    final UUID inId,
    final OBNodeMetadata inNodeMetadata)
  {
    this.graph =
      Objects.requireNonNull(inGraph, "graph");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.id =
      Objects.requireNonNull(inId, "id");
    this.nodeMetadata =
      OBProperty.create(inNodeMetadata);
  }

  @Override
  public final OBPropertyType<OBNodeMetadata> nodeMetadata()
  {
    return this.nodeMetadata;
  }

  @Override
  public final boolean isDeleted()
  {
    return this.graph.nodeIsDeleted(this);
  }

  protected final OBStringsType strings()
  {
    return this.strings;
  }

  @Override
  public final UUID id()
  {
    return this.id;
  }
}
