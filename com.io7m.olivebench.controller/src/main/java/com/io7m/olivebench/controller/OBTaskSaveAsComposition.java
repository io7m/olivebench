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
  private final OBStringsType strings;

  public OBTaskSaveAsComposition(
    final OBController inController,
    final OBCompositionSerializersType inSerializers,
    final OBPreferencesControllerType inPreferences,
    final OBStringsType inStrings,
    final Path inFile)
  {
    this.controller = inController;
    this.serializers = inSerializers;
    this.preferences = inPreferences;
    this.strings = inStrings;
    this.file = inFile;
  }

  public static OBControllerTaskType create(
    final OBServiceDirectoryType services,
    final OBController controller,
    final Path file)
  {
    return new OBTaskSaveAsComposition(
      controller,
      services.requireService(OBCompositionSerializersType.class),
      services.requireService(OBPreferencesControllerType.class),
      services.requireService(OBStringsType.class),
      file
    );
  }

  @Override
  public String name()
  {
    return this.strings.controllerSaveComposition();
  }

  @Override
  public void taskDo()
    throws OBTaskFailureException
  {
    this.controller.publishEvent(
      OBControllerEventTaskProgressChanged.of(
        this.name(),
        this.strings.controllerSaveCompositionSaving(this.file),
        OptionalDouble.empty(),
        OptionalDouble.empty()
      ));

    final var compositionFile =
      this.file.toAbsolutePath();
    final var compositionFileTmp =
      this.file.resolveSibling(compositionFile.getFileName() + ".tmp");

    LOG.debug("write {} (temporary {})", compositionFile, compositionFileTmp);

    try {
      this.serializers.serializeAtomically(
        compositionFile,
        compositionFileTmp,
        this.controller.composition());
      this.controller.setSaved();

      this.preferences.updateQuietly(prefs -> {
        prefs.recentItems().addRecentFile(this.file);
      });
    } catch (final Exception e) {
      LOG.error("i/o error: ", e);
      throw new OBTaskFailureException(
        e,
        OBControllerEventTaskFailed.builder()
          .setTitle(this.strings.controllerOpenCompositionFailed())
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
