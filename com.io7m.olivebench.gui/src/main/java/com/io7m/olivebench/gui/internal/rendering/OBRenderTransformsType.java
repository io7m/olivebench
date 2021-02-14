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

package com.io7m.olivebench.gui.internal.rendering;

import com.io7m.jregions.core.parameterized.areas.PAreaD;
import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.jtensors.core.parameterized.vectors.PVector2D;
import com.io7m.jtensors.core.parameterized.vectors.PVector2L;
import com.io7m.olivebench.composition.spaces.OBScreenSpaceType;
import com.io7m.olivebench.composition.spaces.OBTrackSpaceType;
import com.io7m.olivebench.composition.spaces.OBWorldSpaceType;

public interface OBRenderTransformsType
{
  boolean isVisible(
    PAreaL<OBTrackSpaceType> regionBounds);

  PAreaD<OBScreenSpaceType> screenBounds();

  PAreaD<OBWorldSpaceType> viewportBounds();

  PVector2L<OBTrackSpaceType> worldToTrack(
    PVector2D<OBWorldSpaceType> point);

  PAreaD<OBScreenSpaceType> trackToScreen(
    PAreaL<OBTrackSpaceType> area);

  PVector2D<OBScreenSpaceType> trackToScreen(
    PVector2L<OBTrackSpaceType> point);

  PVector2D<OBWorldSpaceType> screenToWorld(
    PVector2D<OBScreenSpaceType> point);
}
