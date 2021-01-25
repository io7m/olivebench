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

import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.composition.OBDeleteableType;
import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.spaces.OBSpacePatternTrackType;

import java.util.UUID;

/**
 * The base type of regions.
 */

public interface OBRegionType extends OBDeleteableType<OBRegionType>
{
  /**
   * @return The unique region ID
   */

  UUID id();

  /**
   * @return The track to which this region belongs
   */

  OBTrackType track();

  /**
   * @return The bounds of the region
   */

  PAreaL<OBSpacePatternTrackType> bounds();

  /**
   * Set the bounds of the region.
   *
   * @param newBounds The new bounds of the region
   */

  void setBounds(
    PAreaL<OBSpacePatternTrackType> newBounds);
}
