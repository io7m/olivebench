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

import com.io7m.olivebench.composition.regions.OBRegionCurveType;
import com.io7m.olivebench.composition.regions.OBRegionImageType;
import com.io7m.olivebench.composition.regions.OBRegionMusicType;
import com.io7m.olivebench.composition.regions.OBRegionTextType;
import com.io7m.olivebench.composition.regions.OBRegionType;
import com.io7m.olivebench.controller.api.OBControllerReadableType;

import java.util.Objects;

public final class OBRegionOnPatternCanvasRenderer
  implements OBRendererType<OBRegionType>
{
  private final OBRendererType<OBRegionMusicType> musicRenderer;
  private final OBRendererType<OBRegionImageType> imageRenderer;
  private final OBRendererType<OBRegionTextType> textRenderer;
  private final OBRendererType<OBRegionCurveType> curveRenderer;

  public OBRegionOnPatternCanvasRenderer(
    final OBRendererType<OBRegionMusicType> inMusicRenderer,
    final OBRendererType<OBRegionImageType> inImageRenderer,
    final OBRendererType<OBRegionTextType> inTextRenderer,
    final OBRendererType<OBRegionCurveType> inCurveRenderer)
  {
    this.musicRenderer =
      Objects.requireNonNull(inMusicRenderer, "musicRenderer");
    this.imageRenderer =
      Objects.requireNonNull(inImageRenderer, "imageRenderer");
    this.textRenderer =
      Objects.requireNonNull(inTextRenderer, "textRenderer");
    this.curveRenderer =
      Objects.requireNonNull(inCurveRenderer, "curveRenderer");
  }

  public OBRegionOnPatternCanvasRenderer(
    final OBControllerReadableType controller)
  {
    this(
      new OBRegionMusicOnPatternCanvasRenderer(controller),
      new OBRegionImageOnPatternCanvasRenderer(),
      new OBRegionTextOnPatternCanvasRenderer(),
      new OBRegionCurveOnPatternCanvasRenderer()
    );
  }

  @Override
  public void render(
    final OBRenderContextType context,
    final OBRegionType item)
  {
    Objects.requireNonNull(item, "item");

    if (item instanceof OBRegionMusicType) {
      this.musicRenderer.render(context, (OBRegionMusicType) item);
      return;
    }
    if (item instanceof OBRegionImageType) {
      this.imageRenderer.render(context, (OBRegionImageType) item);
      return;
    }
    if (item instanceof OBRegionTextType) {
      this.textRenderer.render(context, (OBRegionTextType) item);
      return;
    }
    if (item instanceof OBRegionCurveType) {
      this.curveRenderer.render(context, (OBRegionCurveType) item);
      return;
    }
    throw new IllegalStateException();
  }
}
