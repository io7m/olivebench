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
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.strings.OBStringsType;

import java.util.Objects;

public final class OBTaskNewComposition implements OBControllerTaskType
{
  private final OBController controller;
  private final OBServiceDirectoryType services;
  private final OBStringsType strings;

  private OBTaskNewComposition(
    final OBServiceDirectoryType inServices,
    final OBStringsType inStrings,
    final OBController inController)
  {
    this.services =
      Objects.requireNonNull(inServices, "inServices");
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
    this.controller =
      Objects.requireNonNull(inController, "inController");
  }

  public static OBControllerTaskType create(
    final OBServiceDirectoryType inServices,
    final OBController inController)
  {
    return new OBTaskNewComposition(
      inServices,
      inServices.requireService(OBStringsType.class),
      inController
    );
  }

  @Override
  public String name()
  {
    return this.strings.controllerNewComposition();
  }

  @Override
  public void taskDo()
  {
    final var composition = OBComposition.create(this.services);
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
