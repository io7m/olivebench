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
import com.io7m.olivebench.composition.OBDurationD;
import com.io7m.olivebench.composition.OBNoteIntervalD;
import com.io7m.olivebench.composition.spaces.OBScreenSpaceType;
import com.io7m.olivebench.composition.spaces.OBTrackSpaceType;
import com.io7m.olivebench.composition.spaces.OBViewSpaceType;
import com.io7m.olivebench.composition.spaces.OBWorldSpaceType;

public final class OBCoordinateSpaces
{
  private OBCoordinateSpaces()
  {

  }

  public static PVector2D<OBWorldSpaceType> trackToWorld(
    final PVector2L<OBTrackSpaceType> point)
  {
    return PVector2D.of(
      (double) point.x(),
      (double) point.y()
    );
  }

  public static PAreaD<OBWorldSpaceType> trackToWorldArea(
    final PAreaL<OBTrackSpaceType> area)
  {
    final var p0 =
      PVector2L.<OBTrackSpaceType>of(area.minimumX(), area.minimumY());
    final var p1 =
      PVector2L.<OBTrackSpaceType>of(area.maximumX(), area.maximumY());
    final var pk0 =
      trackToWorld(p0);
    final var pk1 =
      trackToWorld(p1);
    return PAreaD.of(pk0.x(), pk1.x(), pk0.y(), pk1.y());
  }

  public static PVector2L<OBTrackSpaceType> worldToTrack(
    final PVector2D<OBWorldSpaceType> point)
  {
    return PVector2L.of(
      Math.round(point.x()),
      Math.round(point.y())
    );
  }

  public static PVector2D<OBViewSpaceType> worldToView(
    final PAreaD<OBWorldSpaceType> cameraBounds,
    final PVector2D<OBWorldSpaceType> point)
  {
    final var minX = cameraBounds.minimumX();
    final var minY = cameraBounds.minimumY();
    final var viewX = (point.x() - minX) / cameraBounds.sizeX();
    final var viewY = (point.y() - minY) / cameraBounds.sizeY();
    return PVector2D.of(viewX, viewY);
  }

  public static PVector2D<OBWorldSpaceType> viewToWorld(
    final PAreaD<OBWorldSpaceType> cameraBounds,
    final PVector2D<OBViewSpaceType> point)
  {
    final var minX = cameraBounds.minimumX();
    final var minY = cameraBounds.minimumY();
    final var worldX = (point.x() * cameraBounds.sizeX()) + minX;
    final var worldY = (point.y() * cameraBounds.sizeY()) + minY;
    return PVector2D.of(worldX, worldY);
  }

  public static OBDurationD<OBWorldSpaceType> viewToWorld(
    final PAreaD<OBWorldSpaceType> cameraBounds,
    final OBDurationD<OBViewSpaceType> duration)
  {
    return OBDurationD.of((duration.value() * cameraBounds.sizeX()));
  }

  public static OBNoteIntervalD<OBWorldSpaceType> viewToWorld(
    final PAreaD<OBWorldSpaceType> cameraBounds,
    final OBNoteIntervalD<OBViewSpaceType> interval)
  {
    return OBNoteIntervalD.of((interval.value() * cameraBounds.sizeY()));
  }

  public static PVector2D<OBScreenSpaceType> viewToScreen(
    final PAreaD<OBScreenSpaceType> windowBounds,
    final PVector2D<OBViewSpaceType> point)
  {
    return PVector2D.of(
      point.x() * windowBounds.sizeX(),
      point.y() * windowBounds.sizeY()
    );
  }

  public static PVector2D<OBViewSpaceType> screenToView(
    final PAreaD<OBScreenSpaceType> windowBounds,
    final PVector2D<OBScreenSpaceType> point)
  {
    return PVector2D.of(
      point.x() / windowBounds.sizeX(),
      point.y() / windowBounds.sizeY()
    );
  }

  public static OBDurationD<OBViewSpaceType> screenToView(
    final PAreaD<OBScreenSpaceType> windowBounds,
    final OBDurationD<OBScreenSpaceType> duration)
  {
    return OBDurationD.of(duration.value() / windowBounds.sizeX());
  }

  public static OBNoteIntervalD<OBViewSpaceType> screenToView(
    final PAreaD<OBScreenSpaceType> windowBounds,
    final OBNoteIntervalD<OBScreenSpaceType> interval)
  {
    return OBNoteIntervalD.of(interval.value() / windowBounds.sizeY());
  }

  public static PVector2D<OBScreenSpaceType> trackToScreen(
    final PAreaD<OBScreenSpaceType> windowBounds,
    final PAreaD<OBWorldSpaceType> cameraBounds,
    final PVector2L<OBTrackSpaceType> point)
  {
    return worldToScreen(windowBounds, cameraBounds, trackToWorld(point));
  }

  public static PAreaD<OBScreenSpaceType> trackToScreenArea(
    final PAreaD<OBScreenSpaceType> windowBounds,
    final PAreaD<OBWorldSpaceType> cameraBounds,
    final PAreaL<OBTrackSpaceType> area)
  {
    final var p0 =
      PVector2L.<OBTrackSpaceType>of(area.minimumX(), area.minimumY());
    final var p1 =
      PVector2L.<OBTrackSpaceType>of(area.maximumX(), area.maximumY());
    final var pk0 =
      trackToScreen(windowBounds, cameraBounds, p0);
    final var pk1 =
      trackToScreen(windowBounds, cameraBounds, p1);
    return PAreaD.of(pk0.x(), pk1.x(), pk0.y(), pk1.y());
  }

  public static PVector2D<OBScreenSpaceType> worldToScreen(
    final PAreaD<OBScreenSpaceType> windowBounds,
    final PAreaD<OBWorldSpaceType> cameraBounds,
    final PVector2D<OBWorldSpaceType> point)
  {
    return viewToScreen(windowBounds, worldToView(cameraBounds, point));
  }

  public static PVector2D<OBWorldSpaceType> screenToWorld(
    final PAreaD<OBScreenSpaceType> windowBounds,
    final PAreaD<OBWorldSpaceType> cameraBounds,
    final PVector2D<OBScreenSpaceType> point)
  {
    return viewToWorld(cameraBounds, screenToView(windowBounds, point));
  }

  public static OBDurationD<OBWorldSpaceType> screenToWorld(
    final PAreaD<OBScreenSpaceType> windowBounds,
    final PAreaD<OBWorldSpaceType> cameraBounds,
    final OBDurationD<OBScreenSpaceType> duration)
  {
    return viewToWorld(cameraBounds, screenToView(windowBounds, duration));
  }

  public static OBNoteIntervalD<OBWorldSpaceType> screenToWorld(
    final PAreaD<OBScreenSpaceType> windowBounds,
    final PAreaD<OBWorldSpaceType> cameraBounds,
    final OBNoteIntervalD<OBScreenSpaceType> duration)
  {
    return viewToWorld(cameraBounds, screenToView(windowBounds, duration));
  }
}
