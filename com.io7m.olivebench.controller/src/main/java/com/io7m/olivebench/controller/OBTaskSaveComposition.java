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
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.strings.OBStringsType;

import java.nio.file.Path;

public final class OBTaskSaveComposition implements OBControllerTaskType
{
  private final OBController controller;
  private final OBCompositionSerializersType serializers;
  private final OBPreferencesControllerType preferences;
  private final OBServiceDirectoryType services;
  private final OBStringsType strings;

  private OBTaskSaveComposition(
    final OBServiceDirectoryType inServices,
    final OBController inController,
    final OBStringsType inStrings,
    final OBCompositionSerializersType inSerializers,
    final OBPreferencesControllerType inPreferences)
  {
    this.services = inServices;
    this.strings = inStrings;
    this.controller = inController;
    this.serializers = inSerializers;
    this.preferences = inPreferences;
  }

  public static OBControllerTaskType create(
    final OBServiceDirectoryType services,
    final OBController inController)
  {
    return new OBTaskSaveComposition(
      services,
      inController,
      services.requireService(OBStringsType.class),
      services.requireService(OBCompositionSerializersType.class),
      services.requireService(OBPreferencesControllerType.class)
    );
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
    final var fileName =
      this.controller.composition().fileName().read().orElseThrow();

    OBTaskSaveAsComposition.create(this.services, this.controller, fileName)
      .taskDo();
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
