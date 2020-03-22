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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * The preferences registry.
 */

public interface OBPreferencesReadableType
{
  /**
   * @return Preferences related to the undo history
   */

  OBPreferencesUndoReadableType undo();

  /**
   * @return Preferences related to the recent items
   */

  OBPreferencesRecentItemsReadableType recentItems();

  /**
   * Save the current preferences to the given output stream.
   *
   * @param stream The output stream
   *
   * @throws IOException On I/O errors
   */

  void save(OutputStream stream)
    throws IOException;

  /**
   * Save the current preferences to {@code pathTmp}, atomically replacing
   * {@code path} if saving succeeds.
   *
   * @param path    The final output path
   * @param pathTmp The temporary output path
   *
   * @throws IOException On I/O errors
   */

  default void saveTo(
    final Path path,
    final Path pathTmp)
    throws IOException
  {
    try (var output =
           Files.newOutputStream(pathTmp, WRITE, TRUNCATE_EXISTING, CREATE)) {
      this.save(output);
      output.flush();
      Files.move(pathTmp, path, ATOMIC_MOVE, REPLACE_EXISTING);
    }
  }
}
