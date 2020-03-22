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

import com.io7m.olivebench.composition_serializer.api.OBCompositionSerializersType;
import com.io7m.olivebench.preferences.OBPreferencesControllerType;

public final class OBTaskSaveComposition implements OBControllerTaskType
{
  private final OBController controller;
  private final OBCompositionSerializersType serializers;
  private final OBPreferencesControllerType preferences;

  public OBTaskSaveComposition(
    final OBController inController,
    final OBCompositionSerializersType inSerializers,
    final OBPreferencesControllerType inPreferences)
  {
    this.controller = inController;
    this.serializers = inSerializers;
    this.preferences = inPreferences;
  }

  @Override
  public String name()
  {
    return this.controller.strings().controllerSaveComposition();
  }

  @Override
  public void taskDo()
    throws OBTaskFailureException
  {
    new OBTaskSaveAsComposition(
      this.controller,
      this.serializers,
      this.preferences,
      this.controller.composition().fileName().orElseThrow()
    ).taskDo();
  }

  @Override
  public UndoStyle undoStyle()
  {
    return UndoStyle.CANNOT_UNDO;
  }

  @Override
  public boolean isLongRunning()
  {
    return true;
  }

  @Override
  public void taskUndo()
  {
    throw new UnsupportedOperationException();
  }
}
