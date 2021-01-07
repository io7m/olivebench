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

package com.io7m.olivebench.model.spatial;

import com.io7m.jaffirm.core.Postconditions;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jregions.core.unparameterized.areas.AreaL;
import com.io7m.jspatial.api.quadtrees.QuadTreeConfigurationL;
import com.io7m.jspatial.api.quadtrees.QuadTreeReadableLType;
import com.io7m.jspatial.implementation.QuadTreeL;
import com.io7m.olivebench.model.graph.OBCompositionEdge;
import com.io7m.olivebench.model.graph.OBCompositionNodeType;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

public final class OBQuadTrees
{
  private final Graph<OBCompositionNodeType, OBCompositionEdge> graph;
  private final HashMap<UUID, ProcessedNode> processedNodes;
  private final LinkedList<OBCompositionNodeType> stack;
  private final OBCompositionNodeType root;
  private final int graphSize;
  private long accumXMax;
  private long accumXMin;
  private long accumYMax;
  private long accumYMin;

  public OBQuadTrees(
    final Graph<OBCompositionNodeType, OBCompositionEdge> inGraph,
    final OBCompositionNodeType inRoot)
  {
    this.graph =
      Objects.requireNonNull(inGraph, "graph");
    this.root =
      Objects.requireNonNull(inRoot, "root");
    this.stack =
      new LinkedList<>();
    this.graphSize =
      this.graph.vertexSet().size();
    this.processedNodes =
      new HashMap<>(this.graphSize);
  }

  public QuadTreeReadableLType<OBCompositionNodeType> process()
  {
    this.accumXMin = Long.MAX_VALUE;
    this.accumXMax = Long.MIN_VALUE;
    this.accumYMin = Long.MAX_VALUE;
    this.accumYMax = Long.MIN_VALUE;

    this.processedNodes.clear();
    this.stack.clear();
    this.stack.push(this.root);

    this.processNode(
      new ProcessedNode(this.root, AreaL.of(0L, 0L, 0L, 0L)),
      this.root);

    while (!this.stack.isEmpty()) {
      final var parent = this.stack.pop();
      final var parentNode = this.processedNodes.get(parent.id());
      final var edges = this.graph.outgoingEdgesOf(parent);
      for (final var edge : edges) {
        final var child = edge.nodeTarget();
        this.processNode(parentNode, child);
        this.stack.push(child);
      }
    }

    Postconditions.checkPostconditionV(
      this.graphSize == this.processedNodes.size(),
      "Must have processed %d nodes",
      Integer.valueOf(this.graphSize));

    return this.buildQuadTree();
  }

  private QuadTreeReadableLType<OBCompositionNodeType> buildQuadTree()
  {
    final var treeArea =
      AreaL.of(this.accumXMin, this.accumXMax, this.accumYMin, this.accumYMax);

    final var treeConfig =
      QuadTreeConfigurationL.builder()
        .setArea(treeArea)
        .setMinimumQuadrantHeight(8L)
        .setMinimumQuadrantWidth(4096L)
        .build();

    final var tree = QuadTreeL.<OBCompositionNodeType>create(treeConfig);
    for (final var nodeEntry : this.processedNodes.entrySet()) {
      final var node = nodeEntry.getValue();
      tree.insert(node.node, node.area);
    }

    Postconditions.checkPostconditionV(
      (long) this.graphSize == tree.size(),
      "Must have processed %d nodes",
      Integer.valueOf(this.graphSize));

    return tree;
  }

  private void processNode(
    final ProcessedNode parentNode,
    final OBCompositionNodeType node)
  {
    final var parentArea = parentNode.area;
    final var nodeArea = node.nodeArea();
    final var minX = nodeArea.minimumX() + parentArea.minimumX();
    final var minY = nodeArea.minimumY() + parentArea.minimumY();
    final var maxX = minX + nodeArea.sizeX();
    final var maxY = minY + nodeArea.sizeY();
    final var newArea = AreaL.of(minX, maxX, minY, maxY);

    final var nodeId = node.id();
    Preconditions.checkPreconditionV(
      !this.processedNodes.containsKey(nodeId),
      "Node %s cannot already have been processed",
      nodeId);

    this.accumXMin = Math.min(minX, this.accumXMin);
    this.accumXMax = Math.max(maxX, this.accumXMax);
    this.accumYMin = Math.min(minY, this.accumYMin);
    this.accumYMax = Math.max(maxY, this.accumYMax);
    this.processedNodes.put(nodeId, new ProcessedNode(node, newArea));
  }

  private static final class ProcessedNode
  {
    private final OBCompositionNodeType node;
    private final AreaL area;

    ProcessedNode(
      final OBCompositionNodeType inNode,
      final AreaL inArea)
    {
      this.area = inArea;
      this.node = inNode;
    }
  }
}
