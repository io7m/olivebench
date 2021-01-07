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

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.olivebench.model.OBNoteIndexType;
import com.io7m.olivebench.model.OBTimeTicksType;
import org.immutables.value.Value;

import java.util.Comparator;

/**
 * The type of notes.
 */

@ImmutablesStyleType
@Value.Immutable
public interface OBNoteType extends Comparable<OBNoteType>
{
  /**
   * @return The signed note index
   */

  @Value.Parameter
  @OBNoteIndexType
  long note();

  /**
   * @return The start time of the note in ticks
   */

  @Value.Parameter
  @OBTimeTicksType
  long start();

  /**
   * @return The length of the note in ticks
   */

  @Value.Parameter
  @OBTimeTicksType
  long length();

  @Override
  default int compareTo(final OBNoteType other)
  {
    return Comparator.comparingLong(OBNoteType::note)
      .thenComparingLong(OBNoteType::start)
      .thenComparingLong(OBNoteType::length)
      .compare(this, other);
  }
}
