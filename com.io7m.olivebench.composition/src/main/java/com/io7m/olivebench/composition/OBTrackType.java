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

package com.io7m.olivebench.composition;

import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.composition.regions.OBRegionMusicType;
import com.io7m.olivebench.composition.regions.OBRegionTextType;
import com.io7m.olivebench.composition.regions.OBRegionType;
import com.io7m.olivebench.composition.spaces.OBSpacePatternTrackType;
import net.jcip.annotations.ThreadSafe;

import java.util.Map;
import java.util.UUID;

/**
 * The type of tracks.
 */

@ThreadSafe
public interface OBTrackType extends OBDeleteableType<OBTrackType>
{
  /**
   * @return The unique track ID
   */

  UUID id();

  /**
   * @return The composition to which the track belongs
   */

  OBCompositionType composition();

  /**
   * @return The track metadata
   */

  OBTrackMetadata metadata();

  /**
   * Set the track metadata.
   *
   * @param metadata The new metadata
   */

  void setMetadata(
    OBTrackMetadata metadata);

  /**
   * @return A read-only view of the regions within the track
   */

  Map<UUID, OBRegionType> regions();

  /**
   * Create a new music region.
   *
   * @param bounds        The initial region bounds
   * @param timeSignature The time signature
   * @param keySignature  The key signature
   *
   * @return A new music region
   */

  OBRegionMusicType createMusicRegion(
    PAreaL<OBSpacePatternTrackType> bounds,
    OBTimeSignature timeSignature,
    OBKeySignature keySignature
  );

  /**
   * Create a new music region with the specified ID.
   *
   * @param regionId      The region ID
   * @param bounds        The initial region bounds
   * @param timeSignature The time signature
   * @param keySignature  The key signature
   *
   * @return A new music region
   */

  OBRegionMusicType createMusicRegion(
    UUID regionId,
    PAreaL<OBSpacePatternTrackType> bounds,
    OBTimeSignature timeSignature,
    OBKeySignature keySignature
  );

  /**
   * Create a new text region.
   *
   * @param bounds The initial region bounds
   *
   * @return A new music region
   */

  OBRegionTextType createTextRegion(
    PAreaL<OBSpacePatternTrackType> bounds
  );

  /**
   * Create a new text region with the specified ID.
   *
   * @param regionId The region ID
   * @param bounds   The initial region bounds
   *
   * @return A new text region
   */

  OBRegionTextType createTextRegion(
    UUID regionId,
    PAreaL<OBSpacePatternTrackType> bounds
  );
}
