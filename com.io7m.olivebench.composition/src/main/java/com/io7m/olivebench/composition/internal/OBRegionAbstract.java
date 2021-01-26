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
import com.io7m.olivebench.composition.OBCompositionEventType;
import com.io7m.olivebench.composition.OBCompositionModifiedEvent;
import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.regions.OBRegionType;
import com.io7m.olivebench.composition.spaces.OBSpacePatternTrackType;

import java.util.Objects;
import java.util.UUID;

import static com.io7m.olivebench.composition.OBCompositionChange.REGION_MODIFIED;

public abstract class OBRegionAbstract implements OBRegionType
{
  private final UUID id;
  private final OBTrack track;
  private volatile PAreaL<OBSpacePatternTrackType> bounds;

  protected OBRegionAbstract(
    final OBTrack inTrack,
    final UUID inRegionId,
    final PAreaL<OBSpacePatternTrackType> inBounds)
  {
    this.bounds =
      Objects.requireNonNull(inBounds, "bounds");
    this.id =
      Objects.requireNonNull(inRegionId, "id");
    this.track =
      Objects.requireNonNull(inTrack, "track");
  }

  @Override
  public final boolean isDeleted()
  {
    return this.track.regionIsDeleted(this);
  }

  @Override
  public final void delete()
    throws IllegalStateException
  {
    this.track.regionDelete(this);
  }

  @Override
  public final void undelete()
    throws IllegalStateException
  {
    this.track.regionUndelete(this);
  }

  @Override
  public final UUID id()
  {
    return this.id;
  }

  @Override
  public final OBTrackType track()
  {
    return this.track;
  }

  @Override
  public final PAreaL<OBSpacePatternTrackType> bounds()
  {
    return this.bounds;
  }

  @Override
  public final void setBounds(
    final PAreaL<OBSpacePatternTrackType> newBounds)
  {
    this.bounds = Objects.requireNonNull(newBounds, "newBounds");
    this.publish(OBCompositionModifiedEvent.of(REGION_MODIFIED, this.id));
  }

  protected final void publish(
    final OBCompositionEventType event)
  {
    this.track.publish(event);
  }
}
