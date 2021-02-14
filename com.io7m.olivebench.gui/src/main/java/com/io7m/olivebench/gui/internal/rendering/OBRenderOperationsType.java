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
import com.io7m.jtensors.core.parameterized.vectors.PVector2D;
import com.io7m.jtensors.core.parameterized.vectors.PVector2L;
import com.io7m.jtensors.core.parameterized.vectors.PVector4D;
import com.io7m.olivebench.composition.spaces.OBRGBASpaceType;
import com.io7m.olivebench.composition.spaces.OBScreenSpaceType;
import com.io7m.olivebench.composition.spaces.OBTrackSpaceType;
import com.io7m.olivebench.composition.spaces.OBWorldSpaceType;

import java.util.Optional;

public interface OBRenderOperationsType
{
  void drawRectScreen(
    PAreaD<OBScreenSpaceType> area,
    Optional<PVector4D<OBRGBASpaceType>> stroke,
    Optional<PVector4D<OBRGBASpaceType>> fill);

  void drawRectRoundedScreen(
    PAreaD<OBScreenSpaceType> area,
    Optional<PVector4D<OBRGBASpaceType>> stroke,
    Optional<PVector4D<OBRGBASpaceType>> fill);

  void drawLineTrack(
    PVector4D<OBRGBASpaceType> color,
    PVector2L<OBTrackSpaceType> p0,
    PVector2L<OBTrackSpaceType> p1);

  void drawLineWorld(
    PVector4D<OBRGBASpaceType> color,
    PVector2D<OBWorldSpaceType> p0,
    PVector2D<OBWorldSpaceType> p1);

  void drawLineScreen(
    PVector4D<OBRGBASpaceType> color,
    PVector2D<OBScreenSpaceType> p0,
    PVector2D<OBScreenSpaceType> p1);

  void drawTextScreen(
    PVector4D<OBRGBASpaceType> color,
    PVector2D<OBScreenSpaceType> position,
    String text,
    double size,
    double maxWidth);
}
