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

import com.io7m.jregions.core.parameterized.areas.PAreaD;
import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.jregions.core.parameterized.areas.PAreasD;
import com.io7m.jtensors.core.parameterized.vectors.PVector2D;
import com.io7m.jtensors.core.parameterized.vectors.PVector2L;
import com.io7m.jtensors.core.parameterized.vectors.PVector4D;
import com.io7m.jtensors.core.parameterized.vectors.PVectors2D;
import com.io7m.olivebench.composition.spaces.OBRGBASpaceType;
import com.io7m.olivebench.composition.spaces.OBScreenSpaceType;
import com.io7m.olivebench.composition.spaces.OBTrackSpaceType;
import com.io7m.olivebench.composition.spaces.OBWorldSpaceType;
import com.io7m.olivebench.controller.api.OBControllerReadableType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Objects;
import java.util.Optional;

public final class OBRenderContext implements OBRenderContextType
{
  private final OBControllerReadableType controller;
  private final Canvas canvas;
  private final GraphicsContext graphics;

  public OBRenderContext(
    final OBControllerReadableType inController,
    final Canvas inCanvas)
  {
    this.controller =
      Objects.requireNonNull(inController, "controller");
    this.canvas =
      Objects.requireNonNull(inCanvas, "canvas");
    this.graphics =
      this.canvas.getGraphicsContext2D();
  }

  @Override
  public void drawRectScreen(
    final PAreaD<OBScreenSpaceType> area,
    final Optional<PVector4D<OBRGBASpaceType>> stroke,
    final Optional<PVector4D<OBRGBASpaceType>> fill)
  {
    this.graphics.save();
    try {
      final var w = (double) Math.round(area.sizeX());
      final var h = (double) Math.round(area.sizeY());
      final var x0 = 0.5 + (double) Math.round(area.minimumX());
      final var y0 = 0.5 + (double) Math.round(area.minimumY());

      fill.ifPresent(fillColor -> {
        this.graphics.setFill(Color.color(
          fillColor.x(),
          fillColor.y(),
          fillColor.z(),
          fillColor.w()
        ));
        this.graphics.fillRect(x0, y0, w, h);
      });
      stroke.ifPresent(strokeColor -> {
        this.graphics.setStroke(Color.color(
          strokeColor.x(),
          strokeColor.y(),
          strokeColor.z(),
          strokeColor.w()
        ));
        this.graphics.strokeRect(x0, y0, w, h);
      });
    } finally {
      this.graphics.restore();
    }
  }

  @Override
  public void drawRectRoundedScreen(
    final PAreaD<OBScreenSpaceType> area,
    final Optional<PVector4D<OBRGBASpaceType>> stroke,
    final Optional<PVector4D<OBRGBASpaceType>> fill)
  {
    final var arc = 4.0;

    this.graphics.save();
    try {
      final var w = (double) Math.round(area.sizeX());
      final var h = (double) Math.round(area.sizeY());
      final var x0 = 0.5 + (double) Math.round(area.minimumX());
      final var y0 = 0.5 + (double) Math.round(area.minimumY());

      fill.ifPresent(fillColor -> {
        this.graphics.setFill(Color.color(
          fillColor.x(),
          fillColor.y(),
          fillColor.z(),
          fillColor.w()
        ));
        this.graphics.fillRoundRect(x0, y0, w, h, arc, arc);
      });
      stroke.ifPresent(strokeColor -> {
        this.graphics.setStroke(Color.color(
          strokeColor.x(),
          strokeColor.y(),
          strokeColor.z(),
          strokeColor.w()
        ));
        this.graphics.strokeRoundRect(x0, y0, w, h, arc, arc);
      });
    } finally {
      this.graphics.restore();
    }
  }

  @Override
  public void drawLineTrack(
    final PVector4D<OBRGBASpaceType> color,
    final PVector2L<OBTrackSpaceType> p0,
    final PVector2L<OBTrackSpaceType> p1)
  {
    this.drawLineScreen(
      color,
      OBCoordinateSpaces.trackToScreen(
        this.screenBounds(),
        this.viewportBounds(),
        p0),
      OBCoordinateSpaces.trackToScreen(
        this.screenBounds(),
        this.viewportBounds(),
        p1)
    );
  }

  @Override
  public void drawLineWorld(
    final PVector4D<OBRGBASpaceType> color,
    final PVector2D<OBWorldSpaceType> p0,
    final PVector2D<OBWorldSpaceType> p1)
  {
    this.drawLineScreen(
      color,
      OBCoordinateSpaces.worldToScreen(
        this.screenBounds(),
        this.viewportBounds(),
        p0),
      OBCoordinateSpaces.worldToScreen(
        this.screenBounds(),
        this.viewportBounds(),
        p1)
    );
  }

  @Override
  public void drawLineScreen(
    final PVector4D<OBRGBASpaceType> color,
    final PVector2D<OBScreenSpaceType> p0,
    final PVector2D<OBScreenSpaceType> p1)
  {
    this.graphics.save();
    try {
      this.graphics.setStroke(Color.color(
        color.x(),
        color.y(),
        color.z(),
        color.w()
      ));

      final var x0 = 0.5 + (double) Math.round(p0.x());
      final var y0 = 0.5 + (double) Math.round(p0.y());
      final var x1 = 0.5 + (double) Math.round(p1.x());
      final var y1 = 0.5 + (double) Math.round(p1.y());
      this.graphics.strokeLine(x0, y0, x1, y1);
    } finally {
      this.graphics.restore();
    }
  }

  @Override
  public void drawTextScreen(
    final PVector4D<OBRGBASpaceType> color,
    final PVector2D<OBScreenSpaceType> position,
    final String text,
    final double size,
    final double maxWidth)
  {
    this.graphics.save();
    try {
      this.graphics.setFill(Color.color(
        color.x(),
        color.y(),
        color.z(),
        color.w()
      ));
      this.graphics.setFont(Font.font("Monospaced", size));
      this.graphics.fillText(text, position.x(), position.y(), maxWidth);
    } finally {
      this.graphics.restore();
    }
  }

  @Override
  public boolean isVisible(
    final PAreaL<OBTrackSpaceType> regionBounds)
  {
    final var regionView =
      OBCoordinateSpaces.trackToWorldArea(regionBounds);
    return PAreasD.overlaps(this.viewportBounds(), regionView);
  }

  @Override
  public PAreaD<OBScreenSpaceType> screenBounds()
  {
    return PAreaD.of(
      0.0,
      this.canvas.getWidth(),
      0.0,
      this.canvas.getHeight()
    );
  }

  @Override
  public PAreaD<OBWorldSpaceType> viewportBounds()
  {
    return this.controller.compositionGetViewport();
  }

  @Override
  public PVector2L<OBTrackSpaceType> worldToTrack(
    final PVector2D<OBWorldSpaceType> point)
  {
    return OBCoordinateSpaces.worldToTrack(point);
  }

  @Override
  public PAreaD<OBScreenSpaceType> trackToScreen(
    final PAreaL<OBTrackSpaceType> area)
  {
    return OBCoordinateSpaces.trackToScreenArea(
      this.screenBounds(),
      this.viewportBounds(),
      area
    );
  }

  @Override
  public PVector2D<OBScreenSpaceType> trackToScreen(
    final PVector2L<OBTrackSpaceType> point)
  {
    final var screenBounds =
      this.screenBounds();

    return PVectors2D.clamp(
      OBCoordinateSpaces.trackToScreen(
        screenBounds,
        this.viewportBounds(),
        point
      ),
      PVector2D.of(screenBounds.minimumX(), screenBounds.minimumY()),
      PVector2D.of(screenBounds.maximumX(), screenBounds.maximumY())
    );
  }

  @Override
  public PVector2D<OBWorldSpaceType> screenToWorld(
    final PVector2D<OBScreenSpaceType> point)
  {
    return OBCoordinateSpaces.screenToWorld(
      this.screenBounds(),
      this.viewportBounds(),
      point
    );
  }
}
