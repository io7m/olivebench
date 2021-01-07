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

package com.io7m.olivebench.controller;

import com.io7m.olivebench.model.metadata.OBCompositionMetadata;

import java.util.Objects;
import java.util.function.Function;

public final class OBTaskUpdateMetadata implements OBControllerTaskType
{
  private final OBController controller;
  private final Function<OBCompositionMetadata, OBCompositionMetadata> updater;
  private OBCompositionMetadata existingMetadata;

  public OBTaskUpdateMetadata(
    final OBController inController,
    final Function<OBCompositionMetadata, OBCompositionMetadata> inUpdater)
  {
    this.controller =
      Objects.requireNonNull(inController, "inController");
    this.updater =
      Objects.requireNonNull(inUpdater, "updater");
  }

  @Override
  public String name()
  {
    return this.controller.strings().controllerUpdateMetadata();
  }

  @Override
  public void taskDo()
  {
    final var composition = this.controller.composition();
    this.existingMetadata = composition.metadata().update(this.updater);
  }

  @Override
  public UndoStyle undoStyle()
  {
    return UndoStyle.CAN_UNDO;
  }

  @Override
  public boolean isLongRunning()
  {
    return false;
  }

  @Override
  public void taskUndo()
  {
    final var composition = this.controller.composition();
    composition.metadata().set(this.existingMetadata);
  }
}
