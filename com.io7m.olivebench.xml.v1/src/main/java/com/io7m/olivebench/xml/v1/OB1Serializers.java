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
import com.io7m.jtensors.core.parameterized.vectors.PVector3D;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.olivebench.composition_serializer.spi.OBCompositionSPISerializerType;
import com.io7m.olivebench.composition_serializer.spi.OBCompositionSPISerializersType;
import com.io7m.olivebench.model.OBCompositionReadableType;
import com.io7m.olivebench.model.graph.OBChannelMetadata;
import com.io7m.olivebench.model.graph.OBChannelType;
import com.io7m.olivebench.model.graph.OBCompositionGraphReadableType;
import com.io7m.olivebench.model.graph.OBCompositionNodeType;
import com.io7m.olivebench.model.graph.OBNodeMetadata;
import com.io7m.olivebench.model.graph.OBRegionType;
import com.io7m.olivebench.model.metadata.OBCompositionMetadata;
import com.io7m.olivebench.model.spaces.OBSpaceRGBType;
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

import static com.io7m.olivebench.model.graph.OBRegionType.OBDecorativeRegionType.OBTextRegionType;

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

      this.writeMetadata(writer, this.composition.metadata().read());
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
        this.writeChannel(writer, (OBChannelType) node);
      } else if (node instanceof OBRegionType) {
        this.writeRegion(writer, (OBRegionType<?>) node);
      } else {
        this.writeRoot(writer, node);
      }
    }

    private void writeRoot(
      final XMLStreamWriter writer,
      final OBCompositionNodeType node)
      throws XMLStreamException
    {
      final var nodeMetadata = node.nodeMetadata().read();
      writer.writeStartElement(this.namespace, "Root");
      writer.writeAttribute("id", node.id().toString());
      this.writeNodeMetadata(writer, nodeMetadata);
      writer.writeEndElement();
    }

    private void writeChannel(
      final XMLStreamWriter writer,
      final OBChannelType channel)
      throws XMLStreamException
    {
      final var nodeMetadata = channel.nodeMetadata().read();
      writer.writeStartElement(this.namespace, "Channel");
      writer.writeAttribute("id", channel.id().toString());
      this.writeNodeMetadata(writer, nodeMetadata);
      this.writeChannelMetadata(writer, channel.channelMetadata().read());
      writer.writeEndElement();
    }

    private void writeChannelMetadata(
      final XMLStreamWriter writer,
      final OBChannelMetadata channelMetadata)
      throws XMLStreamException
    {
      writer.writeStartElement(this.namespace, "ChannelMetadata");
      this.writeColor(writer, channelMetadata.color());
      writer.writeEndElement();
    }

    private void writeRegion(
      final XMLStreamWriter writer,
      final OBRegionType<?> region)
      throws XMLStreamException
    {
      if (region instanceof OBTextRegionType) {
        this.writeTextRegion(writer, (OBTextRegionType) region);
        return;
      }
      throw new UnimplementedCodeException();
    }

    private void writeTextRegion(
      final XMLStreamWriter writer,
      final OBTextRegionType region)
      throws XMLStreamException
    {
      final var nodeMetadata = region.nodeMetadata().read();
      writer.writeStartElement(this.namespace, "TextRegion");
      writer.writeAttribute("id", region.id().toString());
      writer.writeAttribute("text", region.text());
      this.writeNodeMetadata(writer, nodeMetadata);
      writer.writeEndElement();
    }

    private void writeNodeMetadata(
      final XMLStreamWriter writer,
      final OBNodeMetadata meta)
      throws XMLStreamException
    {
      writer.writeStartElement(this.namespace, "NodeMetadata");
      writer.writeAttribute("name", meta.name().value());
      this.writeArea(writer, meta.area());
      writer.writeEndElement();
    }

    private void writeColor(
      final XMLStreamWriter writer,
      final PVector3D<OBSpaceRGBType> color)
      throws XMLStreamException
    {
      writer.writeStartElement(this.namespace, "Color");
      writer.writeAttribute("red", Double.toString(color.x()));
      writer.writeAttribute("green", Double.toString(color.y()));
      writer.writeAttribute("blue", Double.toString(color.z()));
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
      final OBCompositionMetadata metadata)
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
}
