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
import com.io7m.olivebench.model.spaces.OBSpaceRegionType;
import com.io7m.olivebench.strings.OBStringsType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

import java.util.Objects;
import java.util.UUID;

public abstract class OBAbstractNode implements OBCompositionNodeType
{
  private final OBCompositionGraphType graph;
  private final OBStringsType strings;
  private final Subject<OBName> nameSubject;
  private final Subject<PAreaL<OBSpaceRegionType>> areaSubject;
  private final UUID id;
  private volatile OBName name;
  private volatile PAreaL<OBSpaceRegionType> area;

  protected OBAbstractNode(
    final OBCompositionGraphType inGraph,
    final OBStringsType inStrings,
    final UUID inId)
  {
    this(
      inGraph,
      inStrings,
      inId,
      PAreaL.of(0L, 0L, 0L, 0L),
      OBName.of(""));
  }

  protected OBAbstractNode(
    final OBCompositionGraphType inGraph,
    final OBStringsType inStrings,
    final UUID inId,
    final PAreaL<OBSpaceRegionType> inArea,
    final OBName inName)
  {
    this.graph =
      Objects.requireNonNull(inGraph, "graph");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.id =
      Objects.requireNonNull(inId, "id");
    this.area =
      Objects.requireNonNull(inArea, "area");
    this.name =
      Objects.requireNonNull(inName, "name");

    this.nameSubject =
      PublishSubject.<OBName>create().toSerialized();
    this.areaSubject =
      PublishSubject.<PAreaL<OBSpaceRegionType>>create().toSerialized();
  }

  @Override
  public final Observable<PAreaL<OBSpaceRegionType>> areaRelativeProperty()
  {
    return this.areaSubject;
  }

  @Override
  public final Observable<OBName> nameProperty()
  {
    return this.nameSubject;
  }

  @Override
  public final OBName name()
  {
    return this.name;
  }

  @Override
  public final void setName(final OBName inName)
  {
    this.name = Objects.requireNonNull(inName, "name");
    this.nameSubject.onNext(this.name);
  }

  @Override
  public final void setAreaRelative(
    final PAreaL<OBSpaceRegionType> newArea)
  {
    this.area = Objects.requireNonNull(newArea, "newArea");
    this.areaSubject.onNext(this.area);
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
  public final PAreaL<OBSpaceRegionType> areaRelative()
  {
    return this.area;
  }

  @Override
  public final UUID id()
  {
    return this.id;
  }
}
