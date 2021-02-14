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

import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyIncorrectType;
import com.io7m.jproperties.JPropertyNonexistent;
import com.io7m.jtensors.core.parameterized.vectors.PVector4D;
import com.io7m.olivebench.composition.OBDublinCoreMetadata;
import com.io7m.olivebench.composition.spaces.OBRGBASpaceType;
import com.io7m.olivebench.preferences.api.OBPreferences;
import com.io7m.olivebench.preferences.api.OBRecentFiles;
import com.io7m.olivebench.theme.api.OBTheme;
import com.io7m.olivebench.theme.api.OBThemeSilver;
import org.slf4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.io7m.olivebench.theme.api.OBThemeType.THEME_PROPERTY_NAMES;

public final class OBPreferencesLoader
{
  private static final Pattern COLOR_PATTERN =
    Pattern.compile(
      "#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})",
      Pattern.CASE_INSENSITIVE);

  private final Logger logger;
  private final Path file;
  private final Properties properties;

  public OBPreferencesLoader(
    final Logger inLogger,
    final Path inFile,
    final Properties inProperties)
  {
    this.logger =
      Objects.requireNonNull(inLogger, "logger");
    this.file =
      Objects.requireNonNull(inFile, "file");
    this.properties =
      Objects.requireNonNull(inProperties, "stream");
  }

  public OBPreferences load()
    throws IOException
  {
    final var dublinCoreMetadata =
      this.loadDublinCore();
    final var theme =
      this.loadTheme();
    final var recentFiles =
      this.loadRecentFiles();

    return OBPreferences.builder()
      .setDefaultMetadata(dublinCoreMetadata)
      .setTheme(theme)
      .setRecentFiles(recentFiles)
      .build();
  }

  private OBRecentFiles loadRecentFiles()
  {
    final var limit =
      this.integer("recentFiles.limit", 100);

    final var fileSystem = this.file.getFileSystem();
    final var recentFiles = new LinkedList<Path>();
    for (int index = 0; index < 1000; ++index) {
      final var newName =
        String.format("olivebench.recentFiles.%d", Integer.valueOf(index));
      try {
        if (this.properties.containsKey(newName)) {
          final var path =
            fileSystem.getPath(JProperties.getString(this.properties, newName));
          recentFiles.add(path);
        }
      } catch (final JPropertyNonexistent e) {
        this.logger.error("{}: failed to read a recent file: ", this.file, e);
      }
    }
    return OBRecentFiles.builder()
      .setLimit(limit)
      .setFiles(recentFiles)
      .build();
  }

  private OBTheme loadTheme()
  {
    if (this.properties.containsKey("olivebench.theme.id")) {
      try {
        final var builder = OBTheme.builder();
        builder.setId(this.uuid("theme.id"));
        builder.setName(this.string("theme.name", ""));

        for (final var name : THEME_PROPERTY_NAMES) {
          final var newName = String.format("theme.%s", name);
          builder.putColors(name, this.color(newName));
        }
        return builder.build();
      } catch (final Exception e) {
        this.logger.error("{}: failed to parse a theme: ", this.file, e);
        return OBThemeSilver.get();
      }
    }
    return OBThemeSilver.get();
  }

  private PVector4D<OBRGBASpaceType> color(
    final String name)
    throws Exception
  {
    final var newName =
      String.format("olivebench.%s", name);

    final var colorString =
      JProperties.getString(this.properties, newName);
    final var matcher =
      COLOR_PATTERN.matcher(colorString);

    if (matcher.matches()) {
      final var red =
        Integer.parseUnsignedInt(matcher.group(1), 16);
      final var green =
        Integer.parseUnsignedInt(matcher.group(2), 16);
      final var blue =
        Integer.parseUnsignedInt(matcher.group(3), 16);
      final var alpha =
        Integer.parseUnsignedInt(matcher.group(4), 16);

      return PVector4D.of(
        (double) red / 255.0,
        (double) green / 255.0,
        (double) blue / 255.0,
        (double) alpha / 255.0
      );
    }

    final var lineSeparator = System.lineSeparator();
    throw new JPropertyIncorrectType(
      new StringBuilder(128)
        .append("Value cannot be parsed as a color")
        .append(lineSeparator)
        .append("  Name: ")
        .append(newName)
        .append(lineSeparator)
        .append("  Value: ")
        .append(colorString)
        .append(lineSeparator)
        .toString()
    );
  }

  private UUID uuid(
    final String name)
    throws Exception
  {
    final var newName =
      String.format("olivebench.%s", name);

    try {
      return UUID.fromString(JProperties.getString(this.properties, newName));
    } catch (final JPropertyNonexistent e) {
      this.logger.error(
        "{}: missing a required property: {}", this.file, newName);
      throw e;
    } catch (final IllegalArgumentException e) {
      this.logger.error(
        "{}: malformed UUID value: {}", this.file, newName);
      throw e;
    }
  }

  private String string(
    final String name,
    final String orElse)
  {
    final var newName =
      String.format("olivebench.%s", name);
    try {
      return JProperties.getStringOptional(this.properties, newName, orElse);
    } catch (final JPropertyNonexistent ex) {
      this.logger.error(
        "{}: missing a required property: {}", this.file, newName);
      return orElse;
    }
  }

  private int integer(
    final String name,
    final int orElse)
  {
    final var newName =
      String.format("olivebench.%s", name);
    try {
      return JProperties.getBigIntegerOptional(
        this.properties,
        newName,
        new BigInteger(Integer.toString(orElse))
      ).intValueExact();
    } catch (final JPropertyNonexistent ex) {
      this.logger.error(
        "{}: missing a required property: {}", this.file, newName);
      return orElse;
    } catch (final JPropertyIncorrectType ex) {
      this.logger.error(
        "{}: invalid property: {} ({})", this.file, newName, ex.getMessage());
      return orElse;
    }
  }

  private OBDublinCoreMetadata loadDublinCore()
  {
    return OBDublinCoreMetadata.builder()
      .setCreator(this.string("dublinCore.creator", ""))
      .setRights(this.string("dublinCore.rights", "Public Domain"))
      .build();
  }
}
