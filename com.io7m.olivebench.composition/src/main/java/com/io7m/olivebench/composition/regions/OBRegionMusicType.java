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

package com.io7m.olivebench.composition.regions;

import com.io7m.olivebench.composition.OBKeySignature;
import com.io7m.olivebench.composition.OBTimeSignature;

import java.util.SortedSet;

/**
 * The base type of musical regions.
 */

public interface OBRegionMusicType extends OBRegionType
{
  /**
   * @return The time signature of the region
   */

  OBTimeSignature timeSignature();

  /**
   * @return The key signature of the region
   */

  OBKeySignature keySignature();

  /**
   * Set the time signature of the region.
   *
   * @param timeSignature The time signature
   */

  void setTimeSignature(
    OBTimeSignature timeSignature);

  /**
   * Set the key signature of the region.
   *
   * @param keySignature The key signature
   */

  void setKeySignature(
    OBKeySignature keySignature);

  /**
   * Add a note to the region.
   *
   * @param note The note
   */

  void addNote(
    OBNote note);

  /**
   * @return A read-only view of the notes in the region
   */

  SortedSet<OBNote> notes();

  @Override
  OBRegionMusicType delete();
}
