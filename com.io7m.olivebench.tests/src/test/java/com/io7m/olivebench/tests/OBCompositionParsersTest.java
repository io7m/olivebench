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

package com.io7m.olivebench.tests;

import com.io7m.olivebench.composition_parser.api.OBCompositionParserError;
import com.io7m.olivebench.composition_parser.api.OBCompositionParsers;
import com.io7m.olivebench.composition_parser.api.OBCompositionParsersType;
import com.io7m.olivebench.composition_serializer.api.OBCompositionSerializers;
import com.io7m.olivebench.model.OBCompositionType;
import com.io7m.olivebench.strings.OBStrings;
import com.io7m.olivebench.strings.OBStringsType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class OBCompositionParsersTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBCompositionParsersTest.class);

  private OBCompositionParsersType parsers;
  private OBStringsType strings;
  private Path directory;

  private static void serializeTo(
    final OBCompositionType composition0,
    final Path outputFile)
    throws Exception
  {
    final var outputTmp = outputFile.resolveSibling("output.tmp");
    final var serializers = OBCompositionSerializers.create();
    serializers.serializeAtomically(
      outputFile,
      outputTmp,
      composition0
    );
  }

  private static void logErrors(
    final List<OBCompositionParserError> errors)
  {
    for (final var error : errors) {
      LOG.error("error: {}", error);
    }
  }

  @BeforeEach
  public void testSetup()
    throws IOException
  {
    this.parsers = OBCompositionParsers.create();
    this.strings = OBStrings.of(OBStrings.getResourceBundle());
    this.directory = OBTestDirectories.createTempDirectory();
  }

  @Test
  public void testComposition0()
    throws Exception
  {
    this.roundTrip("testComposition0.xml");
  }

  @Test
  public void testMalformed0()
    throws Exception
  {
    Assertions.assertThrows(Exception.class, () -> {
      this.roundTrip("testMalformed0.xml");
    });
  }

  private void roundTrip(
    final String name)
    throws Exception
  {
    final var path =
      OBTestDirectories.resourceOf(
        OBCompositionParsersTest.class,
        this.directory,
        name);

    final var composition0 = this.parse(path);
    final var outputFile = this.directory.resolve("output.xml");
    serializeTo(composition0, outputFile);
    final var composition1 = this.parse(outputFile);

    Files.copy(outputFile, System.out);

    Assertions.assertEquals(
      composition0.graph().id(),
      composition1.graph().id());
    Assertions.assertEquals(
      composition0.metadata(),
      composition1.metadata());
    Assertions.assertEquals(
      composition0.graph().nodes().keySet(),
      composition1.graph().nodes().keySet());
  }

  private OBCompositionType parse(
    final Path path)
    throws Exception
  {
    try (var stream = Files.newInputStream(path)) {
      try (var parser =
             this.parsers.createParser(this.strings, path.toUri(), stream)) {
        final var result = parser.execute();
        logErrors(parser.errors());
        return result.orElseThrow();
      }
    }
  }
}
