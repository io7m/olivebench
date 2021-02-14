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

package com.io7m.olivebench.gui.internal;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

import java.util.Objects;

public final class OBCanvasPane extends Pane
{
  private final Canvas canvas = new Canvas();
  private final Runnable renderListener;

  public OBCanvasPane(
    final Runnable inRenderListener)
  {
    this.renderListener =
      Objects.requireNonNull(inRenderListener, "runnable");
    this.getChildren().add(this.canvas);
  }

  public Canvas canvas()
  {
    return this.canvas;
  }

  public void invalidate()
  {
    this.renderListener.run();
  }

  @Override
  protected void layoutChildren()
  {
    final int top =
      (int) this.snappedTopInset();
    final int right =
      (int) this.snappedRightInset();
    final int bottom =
      (int) this.snappedBottomInset();
    final int left =
      (int) this.snappedLeftInset();

    final int newWidth =
      (int) this.getWidth() - left - right;
    final int newHeight =
      (int) this.getHeight() - top - bottom;

    this.canvas.setLayoutX((double) left);
    this.canvas.setLayoutY((double) top);

    final var oldWidth =
      (int) this.canvas.getWidth();
    final var oldHeight =
      (int) this.canvas.getHeight();

    if (newWidth != oldWidth || newHeight != oldHeight) {
      this.canvas.setWidth((double) newWidth);
      this.canvas.setHeight((double) newHeight);
      this.invalidate();
    }
  }
}
