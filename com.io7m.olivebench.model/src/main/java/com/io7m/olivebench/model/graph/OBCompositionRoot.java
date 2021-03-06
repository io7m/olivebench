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

import com.io7m.olivebench.strings.OBStringsType;
import io.reactivex.rxjava3.core.Observable;

import java.util.UUID;

final class OBCompositionRoot extends OBAbstractNode
{
  private final Observable<Object> changes;

  OBCompositionRoot(
    final OBCompositionGraphType inGraph,
    final OBStringsType inStrings,
    final UUID inId)
  {
    super(inGraph, inStrings, inId);
    this.changes = Observable.never();
  }

  @Override
  public String type()
  {
    return this.strings().composition();
  }

  @Override
  public String toString()
  {
    return String.format("[OBComposition %s]", this.id());
  }

  @Override
  public OBCompositionNodeKind kind()
  {
    return OBCompositionNodeKind.ROOT;
  }

  @Override
  public Observable<Object> changes()
  {
    return this.changes;
  }
}
