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

package com.io7m.olivebench.composition.serializer.api;

import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.services.api.OBServiceType;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * A provider of serializers.
 */

public interface OBCompositionSerializersType extends OBServiceType
{
  /**
   * Create a new serializer using whatever is the highest supported format
   * version.
   *
   * @param services    A directory of services
   * @param target      The target URI
   * @param stream      The output stream
   * @param composition The composition
   *
   * @return A new serializer
   *
   * @throws UnsupportedOperationException If no formats are available
   */

  OBCompositionSerializerType createSerializer(
    OBServiceDirectoryType services,
    URI target,
    OutputStream stream,
    OBCompositionType composition)
    throws UnsupportedOperationException;

  /**
   * Create a new serializer using the specific format version given.
   *
   * @param services     A directory of services
   * @param versionMajor The format major version
   * @param versionMinor The format minor version
   * @param target       The target URI
   * @param stream       The output stream
   * @param composition  The composition
   *
   * @return A new serializer
   *
   * @throws UnsupportedOperationException If the given format is not available
   */

  OBCompositionSerializerType createSerializer(
    OBServiceDirectoryType services,
    int versionMajor,
    int versionMinor,
    URI target,
    OutputStream stream,
    OBCompositionType composition)
    throws UnsupportedOperationException;

  /**
   * A convenience method to serialize the given composition atomically to
   * the given output file, using the given temporary file and the highest
   * supported format version. For atomicity, both files should be on the
   * same filesystem.
   *
   * @param services    A directory of services
   * @param output      The output file
   * @param outputTmp   The temporary file
   * @param composition The composition
   *
   * @throws Exception On errors
   */

  default void serializeAtomically(
    final OBServiceDirectoryType services,
    final Path output,
    final Path outputTmp,
    final OBCompositionType composition)
    throws Exception
  {
    try (var stream = Files.newOutputStream(outputTmp, CREATE_NEW, WRITE)) {
      try (var serializer =
             this.createSerializer(
               services,
               outputTmp.toUri(),
               stream,
               composition)) {
        serializer.execute();
        Files.move(outputTmp, output, ATOMIC_MOVE, REPLACE_EXISTING);
      }
    } finally {
      Files.deleteIfExists(outputTmp);
    }
  }
}
