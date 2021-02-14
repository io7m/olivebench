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

package com.io7m.olivebench.tests.preferences;

import com.io7m.olivebench.composition.OBDublinCoreMetadata;
import com.io7m.olivebench.preferences.api.OBPreferences;
import com.io7m.olivebench.preferences.api.OBPreferencesService;
import com.io7m.olivebench.preferences.api.OBRecentFilesUpdates;
import com.io7m.olivebench.tests.OBTestDirectories;
import com.io7m.olivebench.theme.api.OBTheme;
import com.io7m.olivebench.theme.api.OBThemeRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public final class OBPreferencesServiceTest
{
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = OBTestDirectories.createTempDirectory();
  }

  @Test
  public void testNonexistent()
    throws IOException
  {
    final var preferences0 =
      OBPreferencesService.openOrDefault(this.directory.resolve("nonexistent"));
    final var preferences1 =
      OBPreferencesService.openOrDefault(this.directory.resolve("nonexistent"));

    assertEquals(preferences0.preferences(), preferences1.preferences());
  }

  @Test
  public void testReadWrite()
    throws IOException
  {
    final var preferences =
      OBPreferencesService.openOrDefault(
        this.directory.resolve("preferences.xml"));

    final var prefsThen = preferences.preferences();

    preferences.update(p -> {
      return p.withDefaultMetadata(
        OBDublinCoreMetadata.builder()
          .setRights("All Rights Reserved")
          .setCreator("TestBot")
          .build()
      );
    });

    final var prefsNow = preferences.preferences();
    final var metaNow = prefsNow.defaultMetadata();
    assertEquals("All Rights Reserved", metaNow.rights());
    assertEquals("TestBot", metaNow.creator());
    assertNotEquals(prefsThen, prefsNow);

    final var preferences2 =
      OBPreferencesService.openOrDefault(
        this.directory.resolve("preferences.xml"));

    final var prefsNowMore = preferences2.preferences();
    comparePreferences(prefsNow, prefsNowMore);
  }

  @Test
  public void testTheme()
    throws IOException
  {
    final var preferences =
      OBPreferencesService.openOrDefault(
        this.directory.resolve("preferences.xml"));

    final var prefsThen = preferences.preferences();
    preferences.update(p -> {
      return p.withTheme(OBThemeRandom.get());
    });

    final var prefsNow = preferences.preferences();
    assertNotEquals(prefsThen, prefsNow);

    final var preferences2 =
      OBPreferencesService.openOrDefault(
        this.directory.resolve("preferences.xml"));

    final var prefsNowMore = preferences2.preferences();
    comparePreferences(prefsNow, prefsNowMore);
  }

  @Test
  public void testRecentFiles()
    throws IOException
  {
    final var preferences =
      OBPreferencesService.openOrDefault(
        this.directory.resolve("preferences.xml"));

    final var prefsThen = preferences.preferences();
    preferences.update(p -> {
      var r = p.recentFiles();
      r = OBRecentFilesUpdates.addRecentFile(
        r, this.directory.resolve("a.txt"));
      r = OBRecentFilesUpdates.addRecentFile(
        r, this.directory.resolve("b.txt"));
      r = OBRecentFilesUpdates.addRecentFile(
        r, this.directory.resolve("c.txt"));
      return p.withRecentFiles(r);
    });

    final var prefsNow = preferences.preferences();
    assertNotEquals(prefsThen, prefsNow);

    final var preferences2 =
      OBPreferencesService.openOrDefault(
        this.directory.resolve("preferences.xml"));

    final var prefsNowMore = preferences2.preferences();
    comparePreferences(prefsNow, prefsNowMore);
  }

  private static void comparePreferences(
    final OBPreferences prefsNow,
    final OBPreferences prefsNowMore)
  {
    assertEquals(prefsNowMore.recentFiles(), prefsNow.recentFiles());
    assertEquals(prefsNowMore.defaultMetadata(), prefsNow.defaultMetadata());
    compareTheme(prefsNowMore.theme(), prefsNow.theme());
  }

  private static void compareTheme(
    final OBTheme themeA,
    final OBTheme themeB)
  {
    assertEquals(themeA.id(), themeB.id());
    assertEquals(themeA.name(), themeB.name());

    final var colorsA = themeA.colors();
    final var colorsB = themeB.colors();
    assertEquals(colorsA.size(), colorsB.size());

    for (final var entry : colorsA.entrySet()) {
      final var colorB = colorsB.get(entry.getKey());
      final var colorA = entry.getValue();

      assertEquals(
        colorA.x(),
        colorB.x(),
        0.01,
        String.format("Color: %s", entry.getKey())
      );
      assertEquals(
        colorA.y(),
        colorB.y(),
        0.01,
        String.format("Color: %s", entry.getKey())
      );
      assertEquals(
        colorA.z(),
        colorB.z(),
        0.01,
        String.format("Color: %s", entry.getKey())
      );
      assertEquals(
        colorA.w(),
        colorB.w(),
        0.01,
        String.format("Color: %s", entry.getKey())
      );
    }
  }
}
