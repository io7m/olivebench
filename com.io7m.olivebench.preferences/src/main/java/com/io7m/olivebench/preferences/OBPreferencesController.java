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

package com.io7m.olivebench.preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

public final class OBPreferencesController
  implements OBPreferencesControllerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBPreferencesController.class);

  private final OBPreferencesType preferences;
  private final Path file;
  private final Path fileTmp;

  private OBPreferencesController(
    final OBPreferencesType inPreferences,
    final Path inFile,
    final Path inFileTmp)
  {
    this.preferences =
      Objects.requireNonNull(inPreferences, "inPreferences");
    this.file =
      Objects.requireNonNull(inFile, "file");
    this.fileTmp =
      Objects.requireNonNull(inFileTmp, "fileTmp");
  }

  public static OBPreferencesControllerType create(
    final Path file,
    final Path fileTmp)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(fileTmp, "fileTmp");

    final var preferences = OBPreferences.create();
    if (Files.isRegularFile(file)) {
      try (var stream = Files.newInputStream(file)) {
        preferences.load(stream);
      } catch (final IOException e) {
        LOG.error("unable to open preferences: ", e);
      }
    }

    final var controller =
      new OBPreferencesController(preferences, file, fileTmp);

    /*
     * Schedule an initial update that does nothing. This is to ensure that
     * the preferences file is written to storage using the default values
     * if it did not previously exist.
     */

    controller.updateQuietly(prefs -> {
    });
    return controller;
  }

  @Override
  public OBPreferencesReadableType preferences()
  {
    return this.preferences;
  }

  @Override
  public void update(
    final Consumer<OBPreferencesType> updater)
    throws IOException
  {
    Objects.requireNonNull(updater, "updater");

    updater.accept(this.preferences);
    LOG.debug("saving preferences {}", this.file);
    Files.createDirectories(this.file.getParent());
    this.preferences.saveTo(this.file, this.fileTmp);
  }

  @Override
  public void updateQuietly(
    final Consumer<OBPreferencesType> updater)
  {
    try {
      this.update(updater);
    } catch (final IOException e) {
      LOG.error("unable to save preferences: ", e);
    }
  }
}
