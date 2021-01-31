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

package com.io7m.olivebench.tests.xml;

import com.io7m.olivebench.composition.OBClockService;
import com.io7m.olivebench.composition.OBClockServiceType;
import com.io7m.olivebench.composition.OBCompositionFactoryType;
import com.io7m.olivebench.composition.OBCompositions;
import com.io7m.olivebench.composition.OBLocaleService;
import com.io7m.olivebench.composition.OBLocaleServiceType;
import com.io7m.olivebench.composition.parser.api.OBCompositionParsers;
import com.io7m.olivebench.composition.parser.spi.OBCompositionSPIParsersType;
import com.io7m.olivebench.composition.serializer.api.OBCompositionSerializers;
import com.io7m.olivebench.composition.serializer.spi.OBCompositionSPISerializersType;
import com.io7m.olivebench.services.api.OBServiceDirectory;
import com.io7m.olivebench.tests.OBFileAndStream;
import com.io7m.olivebench.tests.OBTestDirectories;
import com.io7m.olivebench.xml.v1.OBCompositionParserV1;
import com.io7m.olivebench.xml.v1.OBCompositionSerializerV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.io7m.olivebench.tests.OBTestDirectories.resourceStreamOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class OBCompositionSerializersTest
{
  private OBCompositionParsers parsers;
  private OBCompositionSerializers serializers;
  private OBServiceDirectory services;
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = OBTestDirectories.createTempDirectory();
    this.parsers = new OBCompositionParsers();
    this.serializers = new OBCompositionSerializers();
    this.services = new OBServiceDirectory();
    this.services.register(
      OBLocaleServiceType.class,
      new OBLocaleService()
    );
    this.services.register(
      OBClockServiceType.class,
      new OBClockService()
    );
    this.services.register(
      OBCompositionFactoryType.class,
      new OBCompositions()
    );
    this.services.register(
      OBCompositionSPIParsersType.class,
      new OBCompositionParserV1()
    );
    this.services.register(
      OBCompositionSPISerializersType.class,
      new OBCompositionSerializerV1()
    );
  }

  @Test
  public void testRoundTripEmpty()
    throws Exception
  {
    this.roundTrip("composition-empty.xml");
  }

  @Test
  public void testRoundTripOneTrack()
    throws Exception
  {
    this.roundTrip("composition-one-track.xml");
  }

  @Test
  public void testRoundTripRegions()
    throws Exception
  {
    this.roundTrip("composition-regions.xml");
  }

  private void roundTrip(
    final String name)
    throws Exception
  {
    final var fileAndStream =
      this.resource(name);

    final var c0 =
      this.parsers.parse(
        this.services,
        URI.create("urn:stdin"),
        fileAndStream.stream()
      );

    this.serializers.serializeAtomically(
      this.services,
      this.directory.resolve("c0.xml"),
      this.directory.resolve("c0.xml.tmp"),
      c0
    );

    final var c1 =
      this.parsers.parse(
        this.services,
        this.directory.resolve("c0.xml")
      );

    this.serializers.serializeAtomically(
      this.services,
      this.directory.resolve("c1.xml"),
      this.directory.resolve("c1.xml.tmp"),
      c1
    );

    assertEquals(
      Files.readString(fileAndStream.path(), UTF_8),
      Files.readString(this.directory.resolve("c0.xml"), UTF_8)
    );
    assertEquals(
      Files.readString(this.directory.resolve("c0.xml"), UTF_8),
      Files.readString(this.directory.resolve("c1.xml"), UTF_8)
    );
  }

  private OBFileAndStream resource(
    final String name)
    throws IOException
  {
    return resourceStreamOf(
      OBCompositionSerializersTest.class,
      this.directory,
      name
    );
  }
}
