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

import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.regions.OBRegionType;
import com.io7m.olivebench.controller.api.OBControllerReadableType;

import java.util.Objects;

public final class OBTrackOnPatternCanvasRenderer
  implements OBRendererType<OBTrackType>
{
  private final OBRendererType<OBRegionType> regionRenderer;

  public OBTrackOnPatternCanvasRenderer(
    final OBRendererType<OBRegionType> inRegionRenderer)
  {
    this.regionRenderer =
      Objects.requireNonNull(inRegionRenderer, "regionRenderer");
  }

  public OBTrackOnPatternCanvasRenderer(
    final OBControllerReadableType controller)
  {
    this(new OBRegionOnPatternCanvasRenderer(controller));
  }

  @Override
  public void render(
    final OBRenderContextType context,
    final OBTrackType track)
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(track, "item");

    final var regions = track.regions().values();
    for (final var region : regions) {
      this.regionRenderer.render(context, region);
    }
  }
}
