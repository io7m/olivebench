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
import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.composition.OBKeySignature;
import com.io7m.olivebench.composition.OBTimeSignature;
import com.io7m.olivebench.composition.OBTrackMetadata;
import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.regions.OBRegionMusicType;
import com.io7m.olivebench.composition.regions.OBRegionTextType;
import com.io7m.olivebench.composition.regions.OBRegionType;
import com.io7m.olivebench.composition.spaces.OBSpacePatternTrackType;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.io7m.olivebench.composition.OBCompositionChange.REGION_CREATED;
import static com.io7m.olivebench.composition.OBCompositionChange.REGION_DELETED;
import static com.io7m.olivebench.composition.OBCompositionChange.TRACK_METADATA_CHANGED;

public final class OBTrack implements OBTrackType
{
  private final OBComposition composition;
  private final UUID id;
  private final ConcurrentSkipListMap<UUID, OBRegionType> regions;
  private final SortedMap<UUID, OBRegionType> regionsRead;
  private volatile OBTrackMetadata metadata;

  public OBTrack(
    final OBComposition inComposition,
    final UUID inId)
  {
    this.composition =
      Objects.requireNonNull(inComposition, "inComposition");
    this.id =
      Objects.requireNonNull(inId, "id");
    this.metadata =
      OBTrackMetadata.builder().build();
    this.regions =
      new ConcurrentSkipListMap<>();
    this.regionsRead =
      Collections.unmodifiableSortedMap(this.regions);
  }

  public void publish(
    final OBCompositionEventType event)
  {
    this.composition.publish(event);
  }

  @Override
  public UUID id()
  {
    return this.id;
  }

  @Override
  public OBCompositionType composition()
  {
    return this.composition;
  }

  @Override
  public OBTrackMetadata metadata()
  {
    return this.metadata;
  }

  @Override
  public void setMetadata(
    final OBTrackMetadata inMetadata)
  {
    this.metadata = Objects.requireNonNull(inMetadata, "metadata");
    this.publish(
      OBCompositionModifiedEvent.of(TRACK_METADATA_CHANGED, this.id()));
  }

  @Override
  public Map<UUID, OBRegionType> regions()
  {
    return this.regionsRead;
  }

  private <T extends OBRegionType> T saveRegion(
    final T newRegion)
  {
    this.regions.put(newRegion.id(), newRegion);
    this.publish(OBCompositionModifiedEvent.of(REGION_CREATED, newRegion.id()));
    return newRegion;
  }

  @Override
  public OBRegionMusicType createMusicRegion(
    final PAreaL<OBSpacePatternTrackType> bounds,
    final OBTimeSignature timeSignature,
    final OBKeySignature keySignature)
  {
    Objects.requireNonNull(bounds, "bounds");
    Objects.requireNonNull(timeSignature, "timeSignature");
    Objects.requireNonNull(keySignature, "keySignature");

    return this.saveRegion(
      this.composition.objects()
        .withFresh(freshId -> new OBRegionMusic(
          this,
          freshId,
          bounds,
          keySignature,
          timeSignature)
        )
    );
  }

  @Override
  public OBRegionMusicType createMusicRegion(
    final UUID regionId,
    final PAreaL<OBSpacePatternTrackType> bounds,
    final OBTimeSignature timeSignature,
    final OBKeySignature keySignature)
  {
    Objects.requireNonNull(regionId, "regionId");
    Objects.requireNonNull(bounds, "bounds");
    Objects.requireNonNull(timeSignature, "timeSignature");
    Objects.requireNonNull(keySignature, "keySignature");

    return this.saveRegion(
      this.composition.objects()
        .withSpecific(
          regionId,
          freshId -> new OBRegionMusic(
            this,
            freshId,
            bounds,
            keySignature,
            timeSignature)
        )
    );
  }

  @Override
  public OBRegionTextType createTextRegion(
    final PAreaL<OBSpacePatternTrackType> bounds)
  {
    Objects.requireNonNull(bounds, "bounds");

    return this.saveRegion(
      this.composition.objects()
        .withFresh(freshId -> new OBRegionText(this, freshId, bounds))
    );
  }

  @Override
  public OBRegionTextType createTextRegion(
    final UUID regionId,
    final PAreaL<OBSpacePatternTrackType> bounds)
  {
    Objects.requireNonNull(regionId, "regionId");
    Objects.requireNonNull(bounds, "bounds");

    return this.saveRegion(
      this.composition.objects()
        .withSpecific(
          regionId,
          freshId -> new OBRegionText(this, freshId, bounds)
        )
    );
  }

  @Override
  public boolean isDeleted()
  {
    return this.composition.trackIsDeleted(this);
  }

  @Override
  public OBTrackType delete()
  {
    return this.composition.trackDelete(this);
  }

  public boolean regionIsDeleted(
    final OBRegionType region)
  {
    return !Objects.equals(this.regions.get(region.id()), region);
  }

  @Override
  public String toString()
  {
    return String.format("[OBTrack %s]", this.id);
  }

  public OBRegionType regionDelete(
    final OBRegionType region)
  {
    final var deleted = this.regions.remove(region.id(), region);
    if (deleted) {
      this.publish(OBCompositionModifiedEvent.of(REGION_DELETED, region.id()));
      return region;
    }

    throw new IllegalStateException(
      this.composition.strings().format("regionAlreadyDeleted", region.id())
    );
  }
}
