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
import com.io7m.olivebench.composition.regions.OBRegionMusicType;
import com.io7m.olivebench.composition.spaces.OBScreenSpaceType;
import com.io7m.olivebench.composition.spaces.OBTrackSpaceType;
import com.io7m.olivebench.controller.api.OBControllerReadableType;

import java.util.Objects;
import java.util.Optional;

public final class OBRegionMusicOnPatternTimelineRenderer
  implements OBRendererType<OBRegionMusicType>
{
  private final OBControllerReadableType controller;

  public OBRegionMusicOnPatternTimelineRenderer(
    final OBControllerReadableType inController)
  {
    this.controller =
      Objects.requireNonNull(inController, "controller");
  }

  @Override
  public void render(
    final OBRenderContextType context,
    final OBRegionMusicType region)
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(region, "item");

    final var track =
      region.track();
    final var timeSignature =
      region.timeSignature();
    final var keySignature =
      region.keySignature();

    final var screenBoundsOfRegion =
      context.trackToScreen(region.bounds());

    final var theme =
      this.controller.theme();

    final var text = new StringBuilder(64);
    text.append(timeSignature.show());
    text.append(", ");
    text.append(keySignature.name());

    var textColor =
      theme.patternRegionText();
    var headerBackground =
      theme.timelineBackground();

    if (!this.controller.trackIsActive(track)) {
      textColor =
        theme.inactiveOf(textColor);
      headerBackground =
        theme.inactiveOf(headerBackground);
    }

    context.drawTextScreen(
      textColor,
      PVector2D.of(
        screenBoundsOfRegion.minimumX() + 8.0,
        screenBoundsOfRegion.minimumY() + 16.0
      ),
      text.toString(),
      10.0,
      screenBoundsOfRegion.sizeX() - 16.0
    );

    context.drawRectScreen(
      screenBoundsOfRegion,
      Optional.of(headerBackground),
      Optional.empty()
    );

    this.renderTimelineBarBeatTicks(context, region);
  }

  private void renderTimelineBarBeatTicks(
    final OBRenderContextType context,
    final OBRegionMusicType region)
  {
    final var timeConfiguration =
      region.track()
        .composition()
        .metadata()
        .timeConfiguration();

    final var bounds =
      region.bounds();
    final var timeSignature =
      region.timeSignature();

    final var ticksPerQuarterNote =
      timeConfiguration.ticksPerQuarterNote();

    final var ticksPerBeat =
      timeSignature.ticksPerBeat(ticksPerQuarterNote);
    final var beatsPerBar =
      timeSignature.beatsPerBar();

    final var theme =
      this.controller.theme();
    final var colorTickBeat =
      theme.timelineTickBeat();
    final var colorTickBar =
      theme.timelineTickBar();

    final var screenHeight =
      context.screenBounds().sizeY();
    final var screenBeatStartY =
      (screenHeight / 8.0) * 6.0;
    final var screenBarStartY =
      (screenHeight / 9.0) * 6.0;

    var beats = 0L;
    for (var x = bounds.minimumX(); x <= bounds.maximumX(); x += ticksPerBeat) {
      final var pointBase =
        PVector2L.<OBTrackSpaceType>of(x, 0L);
      final var screenPointBase =
        context.trackToScreen(pointBase);

      final var screenBeatP0 =
        PVector2D.<OBScreenSpaceType>of(screenPointBase.x(), screenBeatStartY);
      final var screenP1 =
        PVector2D.<OBScreenSpaceType>of(screenPointBase.x(), screenHeight);

      context.drawLineScreen(colorTickBeat, screenBeatP0, screenP1);
      if (beats % (long) beatsPerBar == 0L) {
        final var screenBarP0 =
          PVector2D.<OBScreenSpaceType>of(screenPointBase.x(), screenBarStartY);
        context.drawLineScreen(colorTickBar, screenBarP0, screenP1);
      }
      ++beats;
    }
  }
}
