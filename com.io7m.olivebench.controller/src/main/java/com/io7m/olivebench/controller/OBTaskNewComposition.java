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

import com.io7m.olivebench.model.OBComposition;

import java.util.Objects;

public final class OBTaskNewComposition implements OBControllerTaskType
{
  private final OBController controller;

  public OBTaskNewComposition(
    final OBController inController)
  {
    this.controller =
      Objects.requireNonNull(inController, "inController");
  }

  @Override
  public String name()
  {
    return this.controller.strings().controllerNewComposition();
  }

  @Override
  public void taskDo()
  {
    final var composition = OBComposition.create(this.controller.strings());
    this.controller.setComposition(composition);
  }

  @Override
  public UndoStyle undoStyle()
  {
    return UndoStyle.CLEARS_UNDO_STACK;
  }

  @Override
  public boolean isLongRunning()
  {
    return false;
  }

  @Override
  public void taskUndo()
  {
    throw new UnsupportedOperationException();
  }
}
