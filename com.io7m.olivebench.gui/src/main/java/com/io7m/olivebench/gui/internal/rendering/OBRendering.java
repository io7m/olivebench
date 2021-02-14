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

package com.io7m.olivebench.gui.internal.rendering;

import com.io7m.jtensors.core.parameterized.vectors.PVector2D;
import com.io7m.jtensors.core.parameterized.vectors.PVector2L;
import com.io7m.olivebench.composition.spaces.OBScreenSpaceType;
import com.io7m.olivebench.composition.spaces.OBTrackSpaceType;
import com.io7m.olivebench.theme.api.OBTheme;

public final class OBRendering
{
  private OBRendering()
  {

  }

  public static void renderTickAtZero(
    final OBTheme theme,
    final OBRenderContextType context)
  {
    final var colorTickBar =
      theme.timelineTickBar();
    final var screenHeight =
      context.screenBounds().sizeY();

    final var pointBase =
      PVector2L.<OBTrackSpaceType>of(0L, 0L);
    final var screenPointBase =
      context.trackToScreen(pointBase);
    final var screenP1 =
      PVector2D.<OBScreenSpaceType>of(screenPointBase.x(), screenHeight);

    final var screenBarP0 =
      PVector2D.<OBScreenSpaceType>of(screenPointBase.x(), 0.0);
    context.drawLineScreen(colorTickBar, screenBarP0, screenP1);
  }
}