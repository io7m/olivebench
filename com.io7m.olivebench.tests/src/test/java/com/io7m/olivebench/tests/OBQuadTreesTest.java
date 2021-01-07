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

import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.model.graph.OBCompositionEdge;
import com.io7m.olivebench.model.graph.OBCompositionNodeType;
import com.io7m.olivebench.model.spaces.OBSpaceRegionType;
import com.io7m.olivebench.model.spatial.OBQuadTrees;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

public final class OBQuadTreesTest
{
  private static OBCompositionNodeType nodeOf(
    final long minX,
    final long maxX,
    final long minY,
    final long maxY)
  {
    final var node =
      Mockito.mock(OBCompositionNodeType.class);
    final var nodeArea =
      PAreaL.<OBSpaceRegionType>of(minX, maxX, minY, maxY);
    final var nodeId =
      UUID.randomUUID();

    Mockito.when(node.nodeArea()).thenReturn(nodeArea);
    Mockito.when(node.id()).thenReturn(nodeId);
    return node;
  }

  @Test
  public void testOnlyRoot()
  {
    final var graph =
      new DirectedAcyclicGraph<OBCompositionNodeType, OBCompositionEdge>(
        OBCompositionEdge.class);

    final var root =
      Mockito.mock(OBCompositionNodeType.class);
    final var rootArea =
      PAreaL.<OBSpaceRegionType>of(0L, 1L, 0L, 1L);

    Mockito.when(root.nodeArea()).thenReturn(rootArea);
    graph.addVertex(root);

    final var trees = new OBQuadTrees(graph, root);
    final var tree = trees.process();
    Assertions.assertEquals(1L, tree.size());
    Assertions.assertTrue(tree.contains(root));
  }

  @Test
  public void testSimple0()
  {
    final var graph =
      new DirectedAcyclicGraph<OBCompositionNodeType, OBCompositionEdge>(
        OBCompositionEdge.class);

    final var root =
      nodeOf(0L, 0L, 0L, 0L);
    final var node0 =
      nodeOf(-100L, 100L, 20L, 40L);
    final var node1 =
      nodeOf(10L, 20L, 5L, 10L);
    final var node2 =
      nodeOf(-10L, 10L, 5L, 10L);

    graph.addVertex(root);
    graph.addVertex(node0);
    graph.addVertex(node1);
    graph.addVertex(node2);
    graph.addEdge(root, node0, OBCompositionEdge.of(root, node0));
    graph.addEdge(node0, node1, OBCompositionEdge.of(node0, node1));
    graph.addEdge(node0, node2, OBCompositionEdge.of(node0, node2));

    final var trees = new OBQuadTrees(graph, root);
    final var tree = trees.process();

    final var treeBounds = tree.bounds();
    Assertions.assertEquals(-110L, treeBounds.minimumX());
    Assertions.assertEquals(100L, treeBounds.maximumX());
    Assertions.assertEquals(0L, treeBounds.minimumY());
    Assertions.assertEquals(40L, treeBounds.maximumY());

    Assertions.assertEquals(4L, tree.size());
    Assertions.assertTrue(tree.contains(root));
    Assertions.assertTrue(tree.contains(node0));
    Assertions.assertTrue(tree.contains(node1));
    Assertions.assertTrue(tree.contains(node2));
  }
}
