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

package com.io7m.olivebench.model.graph;

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jaffirm.core.Preconditions;
import org.jgrapht.Graph;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class OBCompositionGraphs
{
  private OBCompositionGraphs()
  {

  }

  static void checkExistsAndNotRoot(
    final OBCompositionNodeType root,
    final Map<UUID, OBCompositionNodeType> nodes,
    final Graph<OBCompositionNodeType, OBCompositionEdge> graph,
    final OBCompositionNodeType node)
  {
    Objects.requireNonNull(root, "root");
    Objects.requireNonNull(nodes, "nodes");
    Objects.requireNonNull(graph, "graph");
    Objects.requireNonNull(node, "node");

    Preconditions.checkPreconditionV(
      !Objects.equals(root, node),
      "Cannot remove the root node");

    final var existingNode = nodes.get(node.id());
    Preconditions.checkPreconditionV(
      existingNode != null,
      "Node %s must exist in the composition graph",
      node);

    Preconditions.checkPreconditionV(
      Objects.equals(existingNode, node),
      "Node %s must be %s",
      existingNode,
      node);

    Preconditions.checkPreconditionV(
      graph.containsVertex(node),
      "Node %s must exist in this graph",
      node);
  }

  static OBChannelType channelOf(
    final OBCompositionNodeType root,
    final Map<UUID, OBCompositionNodeType> nodes,
    final Graph<OBCompositionNodeType, OBCompositionEdge> graph,
    final OBCompositionNodeType node)
  {
    Objects.requireNonNull(root, "root");
    Objects.requireNonNull(nodes, "nodes");
    Objects.requireNonNull(graph, "graph");
    Objects.requireNonNull(node, "node");

    checkExistsAndNotRoot(root, nodes, graph, node);

    if (node instanceof OBChannelType) {
      return (OBChannelType) node;
    }

    var currentNode = node;
    while (true) {
      final var incoming = graph.incomingEdgesOf(currentNode);
      Invariants.checkInvariantV(
        incoming.size() <= 1,
        "Node %s must have <= 1 incoming edge (got %d)",
        node,
        Integer.valueOf(incoming.size()));

      final var edge = incoming.iterator().next();
      final var target = edge.nodeTarget();
      Preconditions.checkPreconditionV(
        Objects.equals(target, node),
        "Node %s must match %s",
        target,
        node);

      if (edge.nodeSource() instanceof OBChannelType) {
        return (OBChannelType) edge.nodeSource();
      }

      currentNode = edge.nodeSource();
    }
  }
}
