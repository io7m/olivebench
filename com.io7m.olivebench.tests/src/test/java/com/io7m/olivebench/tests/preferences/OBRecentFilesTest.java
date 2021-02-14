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

import com.io7m.olivebench.preferences.api.OBRecentFiles;
import com.io7m.olivebench.preferences.api.OBRecentFilesUpdates;
import com.io7m.olivebench.tests.OBTestDirectories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class OBRecentFilesTest
{
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = OBTestDirectories.createTempDirectory();
  }

  @Test
  public void testUpdateRecentFiles()
  {
    final var initial =
      OBRecentFiles.builder()
        .setLimit(3)
        .build();

    assertEquals(List.of(), initial.files());

    var updated =
      OBRecentFilesUpdates.addRecentFile(
        initial,
        this.directory.resolve("a.txt")
      );

    updated =
      OBRecentFilesUpdates.addRecentFile(
        updated,
        this.directory.resolve("b.txt")
      );

    updated =
      OBRecentFilesUpdates.addRecentFile(
        updated,
        this.directory.resolve("c.txt")
      );

    assertEquals(
      List.of(
        this.directory.resolve("c.txt"),
        this.directory.resolve("b.txt"),
        this.directory.resolve("a.txt")
      ),
      updated.files()
    );

    updated =
      OBRecentFilesUpdates.addRecentFile(
        updated,
        this.directory.resolve("d.txt")
      );

    assertEquals(
      List.of(
        this.directory.resolve("d.txt"),
        this.directory.resolve("c.txt"),
        this.directory.resolve("b.txt")
      ),
      updated.files()
    );

    updated =
      OBRecentFilesUpdates.addRecentFile(
        updated,
        this.directory.resolve("c.txt")
      );

    assertEquals(
      List.of(
        this.directory.resolve("c.txt"),
        this.directory.resolve("d.txt"),
        this.directory.resolve("b.txt")
      ),
      updated.files()
    );
  }
}
