/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.olivebench.composition.internal;

import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.composition.ports.OBPortType;

import java.util.Objects;
import java.util.UUID;

public abstract class OBPortAbstract implements OBPortType
{
  private final UUID id;
  private final OBComposition composition;

  protected OBPortAbstract(
    final OBComposition inComposition,
    final UUID inPortId)
  {
    this.id =
      Objects.requireNonNull(inPortId, "id");
    this.composition =
      Objects.requireNonNull(inComposition, "composition");
  }

  @Override
  public final OBCompositionType composition()
  {
    return this.composition;
  }

  @Override
  public final boolean isDeleted()
  {
    return this.composition.portIsDeleted(this);
  }

  @Override
  public final void delete()
    throws IllegalStateException
  {
    this.composition.portDelete(this);
  }

  @Override
  public final void undelete()
    throws IllegalStateException
  {
    this.composition.portUndelete(this);
  }

  @Override
  public final UUID id()
  {
    return this.id;
  }
}
