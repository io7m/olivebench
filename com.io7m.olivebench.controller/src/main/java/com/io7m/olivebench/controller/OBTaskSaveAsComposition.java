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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.OptionalDouble;

public final class OBTaskSaveAsComposition implements OBControllerTaskType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBTaskSaveAsComposition.class);

  private final OBController controller;
  private final OBCompositionSerializersType serializers;
  private final Path file;
  private final OBPreferencesControllerType preferences;

  public OBTaskSaveAsComposition(
    final OBController inController,
    final OBCompositionSerializersType inSerializers,
    final OBPreferencesControllerType inPreferences,
    final Path inFile)
  {
    this.controller = inController;
    this.serializers = inSerializers;
    this.preferences = inPreferences;
    this.file = inFile;
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
    final var strings = this.controller.strings();

    this.controller.publishEvent(
      OBControllerEventTaskProgressChanged.of(
        this.name(),
        strings.controllerSaveCompositionSaving(this.file),
        OptionalDouble.empty(),
        OptionalDouble.empty()
      ));

    final var compositionFile =
      this.file.toAbsolutePath();
    final var compositionFileTmp =
      this.file.resolveSibling(compositionFile.getFileName() + ".tmp");

    LOG.info("write {} (temporary {})", compositionFile, compositionFileTmp);

    try {
      this.serializers.serializeAtomically(
        compositionFile,
        compositionFileTmp,
        this.controller.composition());

      this.preferences.update(prefs -> {
        prefs.recentItems().addRecentFile(this.file);
      });

      this.controller.setSaved();
    } catch (final Exception e) {
      LOG.error("i/o error: ", e);
      throw new OBTaskFailureException(
        e,
        OBControllerEventTaskFailed.builder()
          .setTitle(strings.controllerOpenCompositionFailed())
          .setMessage(e.getMessage())
          .setException(e)
          .build()
      );
    }
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
