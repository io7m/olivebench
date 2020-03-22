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

import com.io7m.blackthorne.api.BTElementHandlerConstructorType;
import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.blackthorne.api.BTElementParsingContextType;
import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.exceptions.OBException;
import com.io7m.olivebench.model.graph.OBCompositionGraph;
import com.io7m.olivebench.model.graph.OBCompositionGraphType;
import com.io7m.olivebench.model.graph.OBCompositionNodeType;
import com.io7m.olivebench.model.graph.OBTextRegion;
import com.io7m.olivebench.model.spaces.OBSpaceRegionType;
import com.io7m.olivebench.strings.OBStringsType;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class OB1GraphParser
  implements BTElementHandlerType<Object, OBCompositionGraphType>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OB1GraphParser.class);

  private final OBStringsType strings;
  private OB1Nodes nodes;
  private OB1Edges edges;

  public OB1GraphParser(
    final OBStringsType inStrings)
  {
    this.strings = Objects.requireNonNull(inStrings, "strings");
  }

  @Override
  public OBCompositionGraphType onElementFinished(
    final BTElementParsingContextType context)
    throws SAXParseException
  {
    try {
      final var root =
        this.nodes.nodes()
          .stream()
          .filter(node -> node instanceof OB1Root)
          .map(node -> (OB1Root) node)
          .findFirst()
          .orElseThrow();

      final var compositionGraph =
        OBCompositionGraph.createWith(this.strings, root.id());

      final var edgeGraph =
        new DirectedAcyclicGraph<UUID, OB1CompositionEdge>(
          OB1CompositionEdge.class);

      edgeGraph.addVertex(root.id());
      for (final var edge : this.edges.edges()) {
        edgeGraph.addVertex(edge.source());
        edgeGraph.addVertex(edge.target());
        edgeGraph.addEdge(edge.source(), edge.target(), edge);
      }

      final var channels =
        this.nodes.nodes()
          .stream()
          .filter(node -> node instanceof OB1Channel)
          .map(node -> (OB1Channel) node)
          .collect(Collectors.toMap(OB1Channel::id, Function.identity()));

      final var regions =
        this.nodes.nodes()
          .stream()
          .filter(node -> node instanceof OB1RegionType)
          .map(node -> (OB1RegionType) node)
          .collect(Collectors.toMap(OB1RegionType::id, Function.identity()));

      final var stack = new LinkedList<OBCompositionNodeType>();
      stack.push(compositionGraph.root());

      final var compositionNodes = compositionGraph.nodes();
      while (!stack.isEmpty()) {
        final var parent = stack.pop();
        final var parentNode = compositionNodes.get(parent.id());
        final var edgesOutgoing = edgeGraph.outgoingEdgesOf(parent.id());
        for (final var edge : edgesOutgoing) {
          final var child = edge.target();
          stack.push(
            this.processNode(
              compositionGraph,
              parentNode,
              channels,
              regions,
              child
            )
          );
        }
      }

      return compositionGraph;
    } catch (final Exception e) {
      throw context.parseException(e);
    }
  }

  private OBCompositionNodeType processNode(
    final OBCompositionGraphType compositionGraph,
    final OBCompositionNodeType parentNode,
    final Map<UUID, OB1Channel> channels,
    final Map<UUID, OB1RegionType> regions,
    final UUID id)
    throws OBException
  {
    final var channel = channels.get(id);
    if (channel != null) {
      Preconditions.checkPreconditionV(
        Objects.equals(channel.id(), id),
        "Channel ID %s must match %s",
        channel.id(),
        id
      );
      return compositionGraph.createChannel(id, channel.name());
    }

    final var region = regions.get(id);
    if (region != null) {
      Preconditions.checkPreconditionV(
        Objects.equals(region.id(), id),
        "Region ID %s must match %s",
        region.id(),
        id
      );

      final var regionArea =
        PAreaL.<OBSpaceRegionType>builder()
          .from(region.area())
          .build();

      if (region instanceof OB1TextRegion) {
        return compositionGraph.createRegion(
          parentNode,
          id,
          regionArea,
          (graph, id1, area) ->
            OBTextRegion.create(graph, this.strings, id1, area)
        );
      }
    }

    throw new UnsupportedOperationException();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    final var namespace = OB1Schemas.NAMESPACE_1_URI.toString();
    return Map.ofEntries(
      Map.entry(
        BTQualifiedName.of(namespace, "Edges"),
        context1 -> new OB1EdgesParser()
      ),
      Map.entry(
        BTQualifiedName.of(namespace, "Nodes"),
        context1 -> new OB1NodesParser()
      )
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof OB1Nodes) {
      this.nodes = (OB1Nodes) result;
    } else if (result instanceof OB1Edges) {
      this.edges = (OB1Edges) result;
    }
  }
}
