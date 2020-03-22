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

import io.reactivex.rxjava3.core.Observable;
import org.jgrapht.Graph;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class OBCompositionGraphSnapshot implements OBCompositionGraphReadableType
{
  private final Observable<OBGraphEventType> events;
  private final Map<UUID, OBCompositionNodeType> nodes;
  private final Graph<OBCompositionNodeType, OBCompositionEdge> graph;
  private final OBCompositionNodeType root;
  private final String type;

  OBCompositionGraphSnapshot(
    final Map<UUID, OBCompositionNodeType> inNodes,
    final Graph<OBCompositionNodeType, OBCompositionEdge> inGraph,
    final OBCompositionNodeType inRoot,
    final String inType)
  {
    this.nodes =
      Objects.requireNonNull(inNodes, "inNodes");
    this.graph =
      Objects.requireNonNull(inGraph, "inGraph");
    this.root =
      Objects.requireNonNull(inRoot, "inRoot");
    this.type =
      Objects.requireNonNull(inType, "inType");

    this.events = Observable.empty();
  }

  @Override
  public Observable<OBGraphEventType> events()
  {
    return this.events;
  }

  @Override
  public Map<UUID, OBCompositionNodeType> nodes()
  {
    return this.nodes;
  }

  @Override
  public Graph<OBCompositionNodeType, OBCompositionEdge> graph()
  {
    return this.graph;
  }

  @Override
  public OBCompositionNodeType root()
  {
    return this.root;
  }

  @Override
  public OBCompositionGraphReadableType snapshot()
  {
    return this;
  }

  @Override
  public OBChannelType channelOf(
    final OBCompositionNodeType node)
  {
    Objects.requireNonNull(node, "node");

    return OBCompositionGraphs.channelOf(
      this.root,
      this.nodes,
      this.graph,
      node
    );
  }

  @Override
  public UUID id()
  {
    return this.root.id();
  }

  @Override
  public String type()
  {
    return this.type;
  }
}
