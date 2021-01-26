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

import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.composition.OBCompositionModifiedEvent;
import com.io7m.olivebench.composition.OBKeySignature;
import com.io7m.olivebench.composition.OBTimeSignature;
import com.io7m.olivebench.composition.regions.OBNote;
import com.io7m.olivebench.composition.regions.OBRegionMusicType;
import com.io7m.olivebench.composition.spaces.OBSpacePatternTrackType;

import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import static com.io7m.olivebench.composition.OBCompositionChange.REGION_MODIFIED;

public final class OBRegionMusic
  extends OBRegionAbstract implements OBRegionMusicType
{
  private final ConcurrentSkipListSet<OBNote> notes;
  private final SortedSet<OBNote> notesRead;
  private volatile OBKeySignature keySignature;
  private volatile OBTimeSignature timeSignature;

  public OBRegionMusic(
    final OBTrack inTrack,
    final UUID inRegionId,
    final PAreaL<OBSpacePatternTrackType> inBounds,
    final OBKeySignature inKeySignature,
    final OBTimeSignature inTimeSignature)
  {
    super(inTrack, inRegionId, inBounds);

    this.keySignature =
      Objects.requireNonNull(inKeySignature, "keySignature");
    this.timeSignature =
      Objects.requireNonNull(inTimeSignature, "timeSignature");
    this.notes =
      new ConcurrentSkipListSet<OBNote>();
    this.notesRead =
      Collections.unmodifiableSortedSet(this.notes);
  }

  @Override
  public OBTimeSignature timeSignature()
  {
    return this.timeSignature;
  }

  @Override
  public OBKeySignature keySignature()
  {
    return this.keySignature;
  }

  @Override
  public void setTimeSignature(
    final OBTimeSignature inTimeSignature)
  {
    this.timeSignature =
      Objects.requireNonNull(inTimeSignature, "timeSignature");
    this.publish(OBCompositionModifiedEvent.of(REGION_MODIFIED, this.id()));
  }

  @Override
  public void setKeySignature(
    final OBKeySignature inKeySignature)
  {
    this.keySignature =
      Objects.requireNonNull(inKeySignature, "keySignature");
    this.publish(OBCompositionModifiedEvent.of(REGION_MODIFIED, this.id()));
  }

  @Override
  public void addNote(
    final OBNote note)
  {
    this.notes.add(Objects.requireNonNull(note, "note"));
    this.publish(OBCompositionModifiedEvent.of(REGION_MODIFIED, this.id()));
  }

  @Override
  public SortedSet<OBNote> notes()
  {
    return this.notesRead;
  }

  @Override
  public String toString()
  {
    return String.format("[OBRegionMusic %s]", this.id());
  }
}
