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

package com.io7m.olivebench.preferences.api.internal;

import com.io7m.jtensors.core.parameterized.vectors.PVector4D;
import com.io7m.olivebench.composition.OBDublinCoreMetadata;
import com.io7m.olivebench.composition.spaces.OBRGBASpaceType;
import com.io7m.olivebench.preferences.api.OBPreferences;
import com.io7m.olivebench.preferences.api.OBRecentFiles;
import com.io7m.olivebench.theme.api.OBTheme;
import com.io7m.olivebench.theme.api.OBThemeType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class OBPreferencesStorer
{
  private final OutputStream stream;
  private final OBPreferences preferences;
  private Properties properties;

  public OBPreferencesStorer(
    final OutputStream inStream,
    final OBPreferences inPreferences)
  {
    this.stream =
      Objects.requireNonNull(inStream, "stream");
    this.preferences =
      Objects.requireNonNull(inPreferences, "preferences");
    this.properties =
      new Properties();
  }

  public void store()
    throws IOException
  {
    this.properties = new Properties();
    this.storeDublinCore(this.preferences.defaultMetadata());
    this.storeTheme(this.preferences.theme());
    this.storeRecentFiles(this.preferences.recentFiles());
    this.properties.storeToXML(this.stream, "", UTF_8);
  }

  private void storeRecentFiles(
    final OBRecentFiles recentFiles)
  {
    this.integer("recentFiles.limit", recentFiles.limit());

    int index = 0;
    for (final var path : recentFiles.files()) {
      this.string(
        String.format("recentFiles.%d", Integer.valueOf(index)),
        path.toString()
      );
      ++index;
    }
  }

  private void storeTheme(
    final OBTheme theme)
  {
    this.string("theme.id", theme.id().toString());
    this.string("theme.name", theme.name());

    for (final var name : OBThemeType.THEME_PROPERTY_NAMES) {
      this.color(String.format("theme.%s", name), theme.color(name));
    }
  }

  private void storeDublinCore(
    final OBDublinCoreMetadata metadata)
  {
    this.string("dublinCore.creator", metadata.creator());
    this.string("dublinCore.rights", metadata.rights());
  }

  private void color(
    final String name,
    final PVector4D<OBRGBASpaceType> value)
  {
    this.properties.setProperty(
      String.format("olivebench.%s", name),
      String.format(
        "#%02x%02x%02x%02x",
        Integer.valueOf((int) (value.x() * 255.0)),
        Integer.valueOf((int) (value.y() * 255.0)),
        Integer.valueOf((int) (value.z() * 255.0)),
        Integer.valueOf((int) (value.w() * 255.0)))
    );
  }

  private void string(
    final String name,
    final String value)
  {
    this.properties.setProperty(
      String.format("olivebench.%s", name),
      value
    );
  }

  private void integer(
    final String name,
    final int limit)
  {
    this.properties.setProperty(
      String.format("olivebench.%s", name),
      Integer.toString(limit)
    );
  }
}
