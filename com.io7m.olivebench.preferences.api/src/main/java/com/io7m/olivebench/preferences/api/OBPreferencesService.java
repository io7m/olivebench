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

package com.io7m.olivebench.preferences.api;

import com.io7m.olivebench.preferences.api.internal.OBPreferencesLoader;
import com.io7m.olivebench.preferences.api.internal.OBPreferencesStorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class OBPreferencesService implements OBPreferencesServiceType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBPreferencesService.class);

  private final Path file;
  private volatile OBPreferences preferences;

  private OBPreferencesService(
    final Path inFile,
    final OBPreferences inPreferences)
  {
    this.file =
      Objects.requireNonNull(inFile, "file");
    this.preferences =
      Objects.requireNonNull(inPreferences, "preferences");
  }

  public static OBPreferencesServiceType openOrDefault(
    final Path file)
    throws IOException
  {
    final var properties = new Properties();
    try (var stream = Files.newInputStream(file)) {
      properties.loadFromXML(stream);
    } catch (final NoSuchFileException e) {
      LOG.info("preferences file {} does not exist, creating a new one", file);
    }

    return new OBPreferencesService(
      file,
      new OBPreferencesLoader(LOG, file, properties).load()
    );
  }

  @Override
  public OBPreferences preferences()
  {
    return this.preferences;
  }

  @Override
  public void save(
    final OBPreferences newPreferences)
    throws IOException
  {
    this.preferences =
      Objects.requireNonNull(newPreferences, "newPreferences");

    final var tmp =
      this.file.resolveSibling(String.format("%s.xml", UUID.randomUUID()));

    try (var stream = Files.newOutputStream(tmp)) {
      new OBPreferencesStorer(stream, this.preferences).store();
    } catch (final Exception e) {
      Files.deleteIfExists(tmp);
      throw e;
    }

    Files.move(tmp, this.file, ATOMIC_MOVE, REPLACE_EXISTING);
  }
}
