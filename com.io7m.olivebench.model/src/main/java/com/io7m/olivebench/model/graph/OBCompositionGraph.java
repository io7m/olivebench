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
import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.exceptions.OBDuplicateException;
import com.io7m.olivebench.exceptions.OBException;
import com.io7m.olivebench.model.OBCompositionEvents;
import com.io7m.olivebench.model.names.OBName;
import com.io7m.olivebench.model.spaces.OBSpaceRegionType;
import com.io7m.olivebench.strings.OBStringsType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import net.jcip.annotations.NotThreadSafe;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@NotThreadSafe
public final class OBCompositionGraph implements OBCompositionGraphType
{
  private final AsUnmodifiableGraph<OBCompositionNodeType, OBCompositionEdge> graphRead;
  private final DirectedAcyclicGraph<OBCompositionNodeType, OBCompositionEdge> graph;
  private final HashMap<UUID, OBCompositionNodeType> nodes;
  private final Map<UUID, OBCompositionNodeType> nodesRead;
  private final OBCompositionEvents eventFactory;
  private final OBCompositionRoot root;
  private final OBStringsType strings;
  private final Subject<OBGraphEventType> eventSubject;

  private OBCompositionGraph(
    final OBStringsType inStrings,
    final UUID inId)
  {
    this.strings = Objects.requireNonNull(inStrings, "strings");
    Objects.requireNonNull(inId, "id");

    this.eventSubject =
      PublishSubject.<OBGraphEventType>create()
        .toSerialized();
    this.eventFactory =
      new OBCompositionEvents(this.strings);

    this.graph =
      new DirectedAcyclicGraph<>(OBCompositionEdge.class);
    this.graphRead =
      new AsUnmodifiableGraph<>(this.graph);

    this.nodes =
      new HashMap<>(1024);
    this.nodesRead =
      Collections.unmodifiableMap(this.nodes);
    this.root =
      new OBCompositionRoot(this, inStrings, inId);

    this.nodes.put(this.root.id(), this.root);
    this.graph.addVertex(this.root);
  }

  public static OBCompositionGraphType create(
    final OBStringsType inStrings)
  {
    return new OBCompositionGraph(inStrings, UUID.randomUUID());
  }

  public static OBCompositionGraphType createWith(
    final OBStringsType strings,
    final UUID id)
  {
    return new OBCompositionGraph(strings, id);
  }

  private void announce(
    final OBGraphEventType event)
  {
    this.eventSubject.onNext(event);
  }

  @Override
  public Observable<OBGraphEventType> events()
  {
    return this.eventSubject;
  }

  @Override
  public Map<UUID, OBCompositionNodeType> nodes()
  {
    return this.nodesRead;
  }

  @Override
  public Graph<OBCompositionNodeType, OBCompositionEdge> graph()
  {
    return this.graphRead;
  }

  @Override
  public OBCompositionNodeType root()
  {
    return this.root;
  }

  @Override
  public OBCompositionGraphReadableType snapshot()
  {
    final DirectedAcyclicGraph<OBCompositionNodeType, OBCompositionEdge> graphCopy =
      new DirectedAcyclicGraph<>(OBCompositionEdge.class);
    Graphs.addGraph(graphCopy, this.graph);

    return new OBCompositionGraphSnapshot(
      Map.copyOf(this.nodes),
      new AsUnmodifiableGraph<>(graphCopy),
      this.root,
      this.type()
    );
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
  public OBChannelType createChannel(
    final UUID channelId,
    final OBName name)
    throws OBException
  {
    Objects.requireNonNull(channelId, "channelId");
    Objects.requireNonNull(name, "name");

    final var existing = this.nodes.get(channelId);
    if (this.nodes.containsKey(channelId)) {
      throw OBDuplicateException.objectDuplicate(
        this.strings,
        existing.type(),
        channelId.toString()
      );
    }

    final var channel = this.makeChannel(name, channelId);
    this.announce(this.eventFactory.graphNodeAdded(this.root, channel));
    return channel;
  }

  @Override
  public OBChannelType createChannel(
    final OBName name)
  {
    Objects.requireNonNull(name, "name");

    final var channel = this.makeChannel(name, this.createId());
    this.announce(this.eventFactory.graphNodeAdded(this.root, channel));
    return channel;
  }

  @Override
  public void nodeDelete(
    final OBCompositionNodeType node)
  {
    Objects.requireNonNull(node, "node");

    OBCompositionGraphs.checkExistsAndNotRoot(
      this.root,
      this.nodes,
      this.graph,
      node
    );

    final var incoming = this.graph.incomingEdgesOf(node);
    Invariants.checkInvariantV(
      incoming.size() == 1,
      "Node %s must have 1 incoming edge (got %d)",
      node,
      Integer.valueOf(incoming.size()));

    final var edge = incoming.iterator().next();
    final var target = edge.nodeTarget();
    Preconditions.checkPreconditionV(
      Objects.equals(target, node),
      "Node %s must match %s",
      target,
      node);

    this.graph.removeVertex(node);
    this.nodes.remove(node.id());
    this.announce(this.eventFactory.graphNodeRemoved(edge.nodeSource(), node));
  }

  @Override
  public boolean nodeIsDeleted(
    final OBCompositionNodeType node)
  {
    Objects.requireNonNull(node, "node");
    return !Objects.equals(this.nodes.get(node.id()), node);
  }

  private OBChannelType makeChannel(
    final OBName name,
    final UUID channelId)
  {
    final var channel =
      OBChannel.create(this, this.strings, channelId, name);
    final var edge = OBCompositionEdge.of(this.root, channel);
    this.graph.addVertex(channel);
    this.graph.addEdge(this.root, channel, edge);
    this.nodes.put(channelId, channel);
    return channel;
  }

  @Override
  public UUID createId()
  {
    while (true) {
      final var newID = UUID.randomUUID();
      if (!this.uuidIsUsed(newID)) {
        return newID;
      }
    }
  }

  private boolean uuidIsUsed(
    final UUID uuid)
  {
    return this.nodes.containsKey(uuid);
  }

  @Override
  public UUID id()
  {
    return this.root.id();
  }

  @Override
  public String type()
  {
    return this.root.type();
  }

  @Override
  public <T extends OBRegionType> T createRegion(
    final OBCompositionNodeType owner,
    final UUID id,
    final PAreaL<OBSpaceRegionType> area,
    final OBRegionConstructorType<T> constructor)
    throws OBException
  {
    Objects.requireNonNull(owner, "owner");
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(area, "area");
    Objects.requireNonNull(constructor, "constructor");

    final var existing = this.nodes.get(id);
    if (this.nodes.containsKey(id)) {
      throw OBDuplicateException.objectDuplicate(
        this.strings,
        existing.type(),
        id.toString()
      );
    }

    final var region = this.makeRegion(owner, area, constructor, id);
    this.announce(this.eventFactory.graphNodeAdded(owner, region));
    return region;
  }

  @Override
  public <T extends OBRegionType> T createRegion(
    final OBCompositionNodeType owner,
    final PAreaL<OBSpaceRegionType> area,
    final OBRegionConstructorType<T> constructor)
  {
    Objects.requireNonNull(owner, "owner");
    Objects.requireNonNull(area, "area");
    Objects.requireNonNull(constructor, "constructor");

    final T region = this.makeRegion(owner, area, constructor, this.createId());
    this.announce(this.eventFactory.graphNodeAdded(owner, region));
    return region;
  }

  private <T extends OBRegionType> T makeRegion(
    final OBCompositionNodeType owner,
    final PAreaL<OBSpaceRegionType> area,
    final OBRegionConstructorType<T> constructor,
    final UUID id)
  {
    final var existingOwner = this.nodes.get(owner.id());
    Preconditions.checkPreconditionV(
      Objects.equals(existingOwner, owner),
      "Node %s must be %s", owner, existingOwner);

    Preconditions.checkPreconditionV(
      (owner instanceof OBChannelType || owner instanceof OBRegionType),
      "Owner %s must be channel or region",
      owner
    );

    final var region = constructor.construct(this, id, area);
    final var edge = OBCompositionEdge.of(owner, region);
    this.graph.addVertex(region);
    this.graph.addEdge(owner, region, edge);
    this.nodes.put(id, region);
    return region;
  }
}
