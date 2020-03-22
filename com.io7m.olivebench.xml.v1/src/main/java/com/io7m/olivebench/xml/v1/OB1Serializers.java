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

package com.io7m.olivebench.xml.v1;


import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.composition_serializer.spi.OBCompositionSPISerializerType;
import com.io7m.olivebench.composition_serializer.spi.OBCompositionSPISerializersType;
import com.io7m.olivebench.model.OBCompositionReadableType;
import com.io7m.olivebench.model.graph.OBChannelType;
import com.io7m.olivebench.model.graph.OBCompositionGraphReadableType;
import com.io7m.olivebench.model.graph.OBCompositionNodeType;
import com.io7m.olivebench.model.graph.OBRegionType;
import com.io7m.olivebench.model.graph.OBTextRegionType;
import com.io7m.olivebench.model.metadata.OBMetadata;
import com.io7m.olivebench.model.spaces.OBSpaceRegionType;

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
import java.util.Objects;

/**
 * A provider of serializers for the 1.0 XML collection format.
 */

public final class OB1Serializers implements OBCompositionSPISerializersType
{
  /**
   * Construct a provider.
   */

  public OB1Serializers()
  {

  }

  @Override
  public int versionMajor()
  {
    return 1;
  }

  @Override
  public int versionMinor()
  {
    return 0;
  }

  @Override
  public OBCompositionSPISerializerType create(
    final URI target,
    final OutputStream output,
    final OBCompositionReadableType collection)
  {
    return new Serializer(target, output, collection);
  }

  private static final class Serializer implements
    OBCompositionSPISerializerType
  {
    private final URI target;
    private final OutputStream output;
    private final OBCompositionReadableType composition;
    private final ByteArrayOutputStream buffer;
    private final String namespace;

    Serializer(
      final URI inTarget,
      final OutputStream inOutput,
      final OBCompositionReadableType inComposition)
    {
      this.target = inTarget;
      this.output = inOutput;
      this.composition = inComposition;
      this.buffer = new ByteArrayOutputStream(1024);

      this.namespace = OB1Schemas.NAMESPACE_1_URI.toString();
    }

    @Override
    public void execute()
      throws Exception
    {
      final var outputs = XMLOutputFactory.newInstance();

      final var writer = outputs.createXMLStreamWriter(this.buffer, "UTF-8");
      writer.writeStartDocument("UTF-8", "1.0");
      writer.setPrefix("ob", this.namespace);
      writer.writeStartElement(this.namespace, "Composition");
      writer.writeNamespace("ob", this.namespace);

      this.writeMetadata(writer, this.composition.metadata());
      this.writeGraph(writer, this.composition.graph());

      writer.writeEndElement();
      writer.writeEndDocument();
      this.buffer.flush();

      final var transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(
        "{http://xml.apache.org/xslt}indent-amount",
        "2");
      transformer.transform(
        new StreamSource(new ByteArrayInputStream(this.buffer.toByteArray())),
        new StreamResult(this.output));

      this.output.flush();
    }

    private void writeGraph(
      final XMLStreamWriter writer,
      final OBCompositionGraphReadableType graph)
      throws XMLStreamException
    {
      writer.writeStartElement(this.namespace, "Graph");
      this.writeGraphNodes(writer, graph);
      this.writeGraphEdges(writer, graph);
      writer.writeEndElement();
    }

    private void writeGraphEdges(
      final XMLStreamWriter writer,
      final OBCompositionGraphReadableType graph)
      throws XMLStreamException
    {
      writer.writeStartElement(this.namespace, "Edges");
      for (final var edge : graph.graph().edgeSet()) {
        writer.writeStartElement(this.namespace, "Edge");
        writer.writeAttribute("source", edge.nodeSource().id().toString());
        writer.writeAttribute("target", edge.nodeTarget().id().toString());
        writer.writeEndElement();
      }
      writer.writeEndElement();
    }

    private void writeGraphNodes(
      final XMLStreamWriter writer,
      final OBCompositionGraphReadableType graph)
      throws XMLStreamException
    {
      writer.writeStartElement(this.namespace, "Nodes");
      this.writeNode(writer, graph.root());
      for (final var entry : graph.nodes().entrySet()) {
        final var node = entry.getValue();
        if (Objects.equals(node, graph.root())) {
          continue;
        }
        this.writeNode(writer, node);
      }
      writer.writeEndElement();
    }

    private void writeNode(
      final XMLStreamWriter writer,
      final OBCompositionNodeType node)
      throws XMLStreamException
    {
      if (node instanceof OBChannelType) {
        writer.writeStartElement(this.namespace, "Channel");
      } else if (node instanceof OBRegionType) {
        final var region = (OBRegionType) node;
        if (region instanceof OBTextRegionType) {
          final var textRegion = (OBTextRegionType) region;
          writer.writeStartElement(this.namespace, "TextRegion");
          writer.writeAttribute("text", textRegion.text());
        }
      } else {
        writer.writeStartElement(this.namespace, "Root");
      }

      writer.writeAttribute("id", node.id().toString());
      writer.writeAttribute("name", node.name().value());
      this.writeArea(writer, node.areaRelative());
      writer.writeEndElement();
    }

    private void writeArea(
      final XMLStreamWriter writer,
      final PAreaL<OBSpaceRegionType> areaRelative)
      throws XMLStreamException
    {
      writer.writeStartElement(this.namespace, "Area");
      writer.writeAttribute("minimumX", Long.toString(areaRelative.minimumX()));
      writer.writeAttribute("minimumY", Long.toString(areaRelative.minimumY()));
      writer.writeAttribute("maximumX", Long.toString(areaRelative.maximumX()));
      writer.writeAttribute("maximumY", Long.toString(areaRelative.maximumY()));
      writer.writeEndElement();
    }

    private void writeMetadata(
      final XMLStreamWriter writer,
      final OBMetadata metadata)
      throws XMLStreamException
    {
      writer.writeStartElement(this.namespace, "Metadata");

      for (final var property : metadata.properties()) {
        writer.writeStartElement(this.namespace, "Property");
        writer.writeAttribute("name", property.name());
        writer.writeAttribute("value", property.value());
        writer.writeEndElement();
      }

      writer.writeEndElement();
    }

    @Override
    public void close()
      throws IOException
    {
      this.output.flush();
      this.output.close();
    }
  }

  @Override
  public String toString()
  {
    return String.format(
      "[%s %d.%d]",
      this.getClass().getCanonicalName(),
      Integer.valueOf(this.versionMajor()),
      Integer.valueOf(this.versionMinor())
    );
  }
}
