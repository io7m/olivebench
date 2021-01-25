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

package com.io7m.olivebench.xml.v1.internal;

import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.jtensors.core.parameterized.vectors.PVector3D;
import com.io7m.olivebench.composition.OBCompositionMetadata;
import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.composition.OBDublinCoreMetadata;
import com.io7m.olivebench.composition.OBKeySignature;
import com.io7m.olivebench.composition.OBSignificantPitchClass;
import com.io7m.olivebench.composition.OBTimeConfiguration;
import com.io7m.olivebench.composition.OBTimeSignature;
import com.io7m.olivebench.composition.OBTrackMetadata;
import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.regions.OBNote;
import com.io7m.olivebench.composition.regions.OBRegionMusicType;
import com.io7m.olivebench.composition.regions.OBRegionTextType;
import com.io7m.olivebench.composition.regions.OBRegionType;
import com.io7m.olivebench.composition.serializer.spi.OBCompositionSPISerializerType;
import com.io7m.olivebench.composition.spaces.OBSpaceRGBAType;
import com.io7m.olivebench.xml.v1.OBSchemas1;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class OB1CompositionSerializer
  implements OBCompositionSPISerializerType
{
  private static final String DUBLIN_CORE = "http://purl.org/dc/elements/1.1/";
  private final URI target;
  private final OutputStream output;
  private final OBCompositionType composition;
  private final String namespace;

  public OB1CompositionSerializer(
    final URI inTarget,
    final OutputStream inOutput,
    final OBCompositionType inComposition)
  {
    this.target =
      Objects.requireNonNull(inTarget, "target");
    this.output =
      Objects.requireNonNull(inOutput, "output");
    this.composition =
      Objects.requireNonNull(inComposition, "composition");
    this.namespace =
      OBSchemas1.namespace1().toString();
  }

  private static void writeDCElement(
    final XMLStreamWriter writer,
    final String name,
    final String value)
    throws XMLStreamException
  {
    writer.writeStartElement("dc", name, DUBLIN_CORE);
    writer.writeCharacters(value);
    writer.writeEndElement();
  }

  @Override
  public void execute()
    throws Exception
  {
    final var buffer = new ByteArrayOutputStream(1024);
    final var outputs = XMLOutputFactory.newInstance();

    final var writer = outputs.createXMLStreamWriter(buffer, "UTF-8");
    writer.writeStartDocument("UTF-8", "1.0");
    writer.setPrefix("ob", this.namespace);
    writer.setPrefix("dc", DUBLIN_CORE);
    writer.writeStartElement(this.namespace, "Composition");
    writer.writeNamespace("ob", this.namespace);
    writer.writeNamespace("dc", DUBLIN_CORE);

    this.writeMetadata(writer, this.composition.metadata());
    this.writeTracks(writer, this.composition.tracks());

    writer.writeEndElement();
    writer.writeEndDocument();
    buffer.flush();

    final var transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(
      "{http://xml.apache.org/xslt}indent-amount",
      "2");

    this.output.write(
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes(UTF_8));
    this.output.write("\n".getBytes(UTF_8));

    transformer.transform(
      new StreamSource(new ByteArrayInputStream(buffer.toByteArray())),
      new StreamResult(this.output));

    this.output.flush();
  }

  private void writeTracks(
    final XMLStreamWriter writer,
    final SortedMap<UUID, OBTrackType> tracks)
    throws XMLStreamException
  {
    final var ids =
      tracks.keySet()
        .stream()
        .map(UUID::toString)
        .sorted()
        .map(UUID::fromString)
        .collect(Collectors.toList());

    writer.writeStartElement(this.namespace, "Tracks");
    for (final var id : ids) {
      this.writeTrack(writer, tracks.get(id));
    }
    writer.writeEndElement();
  }

  private void writeTrack(
    final XMLStreamWriter writer,
    final OBTrackType track)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "Track");
    writer.writeAttribute("id", track.id().toString());
    this.writeTrackMetadata(writer, track.metadata());
    this.writeTrackRegions(writer, track.regions());
    writer.writeEndElement();
  }

  private void writeTrackRegions(
    final XMLStreamWriter writer,
    final Map<UUID, OBRegionType> regions)
    throws XMLStreamException
  {
    final var ids =
      regions.keySet()
        .stream()
        .map(UUID::toString)
        .sorted()
        .map(UUID::fromString)
        .collect(Collectors.toList());

    for (final var id : ids) {
      this.writeTrackRegion(writer, regions.get(id));
    }
  }

  private void writeTrackRegion(
    final XMLStreamWriter writer,
    final OBRegionType value)
    throws XMLStreamException
  {
    if (value instanceof OBRegionMusicType) {
      this.writeTrackRegionMusic(writer, (OBRegionMusicType) value);
    } else if (value instanceof OBRegionTextType) {
      this.writeTrackRegionText(writer, (OBRegionTextType) value);
    } else {
      throw new IllegalStateException();
    }
  }

  private void writeTrackRegionText(
    final XMLStreamWriter writer,
    final OBRegionTextType value)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "RegionText");
    writer.writeAttribute("id", value.id().toString());
    this.writeArea(writer, value.bounds());
    this.writeText(writer, value.text());
    writer.writeEndElement();
  }

  private void writeText(
    final XMLStreamWriter writer,
    final String text)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "Text");
    writer.writeCharacters(text);
    writer.writeEndElement();
  }

  private void writeTrackRegionMusic(
    final XMLStreamWriter writer,
    final OBRegionMusicType value)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "RegionMusic");
    writer.writeAttribute("id", value.id().toString());
    this.writeArea(writer, value.bounds());
    this.writeKeySignature(writer, value.keySignature());
    this.writeTimeSignature(writer, value.timeSignature());
    for (final var note : value.notes()) {
      this.writeNote(writer, note);
    }
    writer.writeEndElement();
  }

  private void writeTimeSignature(
    final XMLStreamWriter writer,
    final OBTimeSignature timeSignature)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "TimeSignature");
    writer.writeAttribute(
      "lowerNumeral",
      String.valueOf(timeSignature.lowerNumeral()));
    writer.writeAttribute(
      "upperNumeral",
      String.valueOf(timeSignature.upperNumeral()));
    writer.writeEndElement();
  }

  private void writeKeySignature(
    final XMLStreamWriter writer,
    final OBKeySignature keySignature)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "KeySignature");
    writer.writeAttribute("name", keySignature.name());
    writer.writeAttribute(
      "notesPerOctave",
      String.valueOf(keySignature.notesPerOctave()));
    writer.writeAttribute(
      "pitchClassOffset",
      String.valueOf(keySignature.pitchClassOffset()));

    for (final var pitchClass : keySignature.significantPitchClasses()) {
      this.writeSignificantPitchClass(writer, pitchClass);
    }
    writer.writeEndElement();
  }

  private void writeSignificantPitchClass(
    final XMLStreamWriter writer,
    final OBSignificantPitchClass pitchClass)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "SignificantPitchClass");
    writer.writeAttribute(
      "pitchClass",
      String.valueOf(pitchClass.pitchClass()));
    writer.writeAttribute(
      "significance",
      pitchClass.significance().name());
    writer.writeEndElement();
  }

  private void writeArea(
    final XMLStreamWriter writer,
    final PAreaL<?> bounds)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "Area");
    writer.writeAttribute("minimumX", String.valueOf(bounds.minimumX()));
    writer.writeAttribute("minimumY", String.valueOf(bounds.minimumY()));
    writer.writeAttribute("maximumX", String.valueOf(bounds.maximumX()));
    writer.writeAttribute("maximumY", String.valueOf(bounds.maximumY()));
    writer.writeEndElement();
  }

  private void writeNote(
    final XMLStreamWriter writer,
    final OBNote note)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "Note");
    writer.writeAttribute("note", Long.toUnsignedString(note.note()));
    writer.writeAttribute("start", Long.toString(note.start()));
    writer.writeAttribute("length", Long.toUnsignedString(note.length()));
    writer.writeEndElement();
  }

  private void writeTrackMetadata(
    final XMLStreamWriter writer,
    final OBTrackMetadata metadata)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "TrackMetadata");
    writer.writeAttribute("name", metadata.name());
    this.writeColor3F(writer, metadata.color());
    writer.writeEndElement();
  }

  private void writeColor3F(
    final XMLStreamWriter writer,
    final PVector3D<OBSpaceRGBAType> color)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "Color3F");
    writer.writeAttribute("red", String.valueOf(color.x()));
    writer.writeAttribute("green", String.valueOf(color.y()));
    writer.writeAttribute("blue", String.valueOf(color.z()));
    writer.writeEndElement();
  }

  private void writeMetadata(
    final XMLStreamWriter writer,
    final OBCompositionMetadata metadata)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "Metadata");
    writer.writeAttribute("id", metadata.id().toString());
    this.writeDCMetadata(writer, metadata.dcMetadata());
    this.writeTimeConfiguration(writer, metadata.timeConfiguration());
    writer.writeEndElement();
  }

  private void writeTimeConfiguration(
    final XMLStreamWriter writer,
    final OBTimeConfiguration timeConfiguration)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "TimeConfiguration");
    writer.writeAttribute(
      "ticksPerQuarterNote",
      Long.toUnsignedString(timeConfiguration.ticksPerQuarterNote()));
    writer.writeEndElement();
  }

  private void writeDCMetadata(
    final XMLStreamWriter writer,
    final OBDublinCoreMetadata dcMetadata)
    throws XMLStreamException
  {
    writer.writeStartElement(this.namespace, "DCMetadata");

    writeDCElement(writer, "contributor", dcMetadata.contributor());
    writeDCElement(writer, "coverage", dcMetadata.coverage());
    writeDCElement(writer, "creator", dcMetadata.creator());
    writeDCElement(writer, "date", dcMetadata.date());
    writeDCElement(writer, "description", dcMetadata.description());
    writeDCElement(writer, "format", dcMetadata.format());
    writeDCElement(writer, "identifier", dcMetadata.identifier());
    writeDCElement(writer, "language", dcMetadata.language());
    writeDCElement(writer, "publisher", dcMetadata.publisher());
    writeDCElement(writer, "relation", dcMetadata.relation());
    writeDCElement(writer, "rights", dcMetadata.rights());
    writeDCElement(writer, "source", dcMetadata.source());
    writeDCElement(writer, "subject", dcMetadata.subject());
    writeDCElement(writer, "title", dcMetadata.title());
    writeDCElement(writer, "type", dcMetadata.type());

    writer.writeEndElement();
  }

  @Override
  public void close()
    throws IOException
  {
    this.output.close();
  }
}
