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

import com.io7m.olivebench.composition.OBCompositionFactoryType;
import com.io7m.olivebench.composition.OBCompositions;
import com.io7m.olivebench.composition.parser.api.OBCompositionParsers;
import com.io7m.olivebench.composition.parser.spi.OBCompositionSPIParsersType;
import com.io7m.olivebench.services.api.OBServiceDirectory;
import com.io7m.olivebench.tests.OBTestDirectories;
import com.io7m.olivebench.xml.v1.OBCompositionParserV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.UUID;

import static com.io7m.olivebench.tests.OBTestDirectories.resourceStreamOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class OBCompositionParsersTest
{
  private OBCompositionParsers parsers;
  private OBServiceDirectory services;
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = OBTestDirectories.createTempDirectory();
    this.parsers = new OBCompositionParsers();
    this.services = new OBServiceDirectory();
    this.services.register(
      OBCompositionFactoryType.class,
      new OBCompositions()
    );
    this.services.register(
      OBCompositionSPIParsersType.class,
      new OBCompositionParserV1()
    );
  }

  @Test
  public void testParseEmpty()
    throws Exception
  {
    final var stream =
      this.resource("composition-empty.xml");
    final var composition =
      this.parsers.parse(this.services, URI.create("urn:stdin"), stream);

    final var meta = composition.metadata();
    assertEquals("778cc84c-5cd8-4dc3-8bb7-648c44ac371c", meta.id().toString());

    final var dcMeta = meta.dcMetadata();
    assertEquals("Example Composition", dcMeta.title());
    assertEquals("Example Creator", dcMeta.creator());
    assertEquals("Subject", dcMeta.subject());
    assertEquals("Example Description", dcMeta.description());
    assertEquals("Example Publisher", dcMeta.publisher());
    assertEquals("Example Contributor", dcMeta.contributor());
    assertEquals("2021-01-23", dcMeta.date());
    assertEquals("Type", dcMeta.type());
    assertEquals("Format", dcMeta.format());
    assertEquals("778cc84c-5cd8-4dc3-8bb7-648c44ac371c", dcMeta.identifier());
    assertEquals("Source", dcMeta.source());
    assertEquals("en", dcMeta.language());
    assertEquals("Relation", dcMeta.relation());
    assertEquals("Coverage", dcMeta.coverage());
    assertEquals("Rights", dcMeta.rights());

    assertEquals(0, composition.tracks().size());
  }

  @Test
  public void testParseOneTrack()
    throws Exception
  {
    final var stream =
      this.resource("composition-one-track.xml");
    final var composition =
      this.parsers.parse(this.services, URI.create("urn:stdin"), stream);

    final var meta = composition.metadata();
    assertEquals("778cc84c-5cd8-4dc3-8bb7-648c44ac371c", meta.id().toString());

    final var dcMeta = meta.dcMetadata();
    assertEquals("Example Composition", dcMeta.title());
    assertEquals("Example Creator", dcMeta.creator());
    assertEquals("Subject", dcMeta.subject());
    assertEquals("Example Description", dcMeta.description());
    assertEquals("Example Publisher", dcMeta.publisher());
    assertEquals("Example Contributor", dcMeta.contributor());
    assertEquals("2021-01-23", dcMeta.date());
    assertEquals("Type", dcMeta.type());
    assertEquals("Format", dcMeta.format());
    assertEquals("778cc84c-5cd8-4dc3-8bb7-648c44ac371c", dcMeta.identifier());
    assertEquals("Source", dcMeta.source());
    assertEquals("en", dcMeta.language());
    assertEquals("Relation", dcMeta.relation());
    assertEquals("Coverage", dcMeta.coverage());
    assertEquals("Rights", dcMeta.rights());

    assertEquals(1, composition.tracks().size());

    final var track =
      composition.tracks()
        .get(UUID.fromString("ae3e5e10-95b3-48a6-9765-d08046bcbdee"));

    final var track0Meta = track.metadata();
    assertEquals("Track 0", track0Meta.name());
    assertEquals(0.7, track0Meta.color().x());
    assertEquals(0.5, track0Meta.color().y());
    assertEquals(0.3, track0Meta.color().z());
  }

  private InputStream resource(
    final String name)
    throws IOException
  {
    return resourceStreamOf(
      OBCompositionParsersTest.class,
      this.directory,
      name
    ).stream();
  }
}
