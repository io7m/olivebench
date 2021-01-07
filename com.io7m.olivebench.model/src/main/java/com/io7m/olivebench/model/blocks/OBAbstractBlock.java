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

package com.io7m.olivebench.model.blocks;

import com.io7m.olivebench.strings.OBStringsType;

import java.util.Objects;
import java.util.UUID;

public abstract class OBAbstractBlock implements OBBlockType
{
  private final OBStringsType strings;
  private final UUID id;

  protected OBAbstractBlock(
    final OBStringsType inStrings,
    final UUID inId)
  {
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.id =
      Objects.requireNonNull(inId, "id");
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
