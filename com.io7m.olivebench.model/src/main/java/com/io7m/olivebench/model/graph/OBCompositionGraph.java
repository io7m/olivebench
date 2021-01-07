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
import com.io7m.olivebench.exceptions.OBDuplicateException;
import com.io7m.olivebench.exceptions.OBException;
import com.io7m.olivebench.model.OBCompositionEvents;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.strings.OBStringsType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
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
  private final HashMap<UUID, Disposable> subscriptions;
  private final HashMap<UUID, OBCompositionNodeType> nodes;
  private final Map<UUID, OBCompositionNodeType> nodesRead;
  private final OBCompositionEvents eventFactory;
  private final OBServiceDirectoryType services;
  private final OBStringsType strings;
  private final Subject<OBGraphEventType> eventSubject;
  private volatile OBCompositionRoot root;

  private OBCompositionGraph(
    final AsUnmodifiableGraph<OBCompositionNodeType, OBCompositionEdge> graphRead,
    final DirectedAcyclicGraph<OBCompositionNodeType, OBCompositionEdge> graph,
    final HashMap<UUID, OBCompositionNodeType> nodes,
    final HashMap<UUID, Disposable> subscriptions,
    final Map<UUID, OBCompositionNodeType> nodesRead,
    final OBCompositionEvents eventFactory,
    final Subject<OBGraphEventType> eventSubject,
    final OBServiceDirectoryType services,
    final OBStringsType strings)
  {
    this.eventFactory =
      Objects.requireNonNull(eventFactory, "eventFactory");
    this.eventSubject =
      Objects.requireNonNull(eventSubject, "eventSubject");
    this.graph =
      Objects.requireNonNull(graph, "graph");
    this.graphRead =
      Objects.requireNonNull(graphRead, "graphRead");
    this.nodes =
      Objects.requireNonNull(nodes, "nodes");
    this.nodesRead =
      Objects.requireNonNull(nodesRead, "nodesRead");
    this.services =
      Objects.requireNonNull(services, "services");
    this.subscriptions =
      Objects.requireNonNull(subscriptions, "subscriptions");
    this.strings =
      Objects.requireNonNull(strings, "strings");
  }

  public static OBCompositionGraphType create(
    final OBServiceDirectoryType services)
  {
    return createWith(services, UUID.randomUUID());
  }

  public static OBCompositionGraphType createWith(
    final OBServiceDirectoryType services,
    final UUID id)
  {
    Objects.requireNonNull(services, "services");
    Objects.requireNonNull(id, "id");

    final var strings =
      services.requireService(OBStringsType.class);

    final var eventSubject =
      PublishSubject.<OBGraphEventType>create().toSerialized();
    final var eventFactory =
      new OBCompositionEvents(strings);

    final var graph =
      new DirectedAcyclicGraph<OBCompositionNodeType, OBCompositionEdge>(
        OBCompositionEdge.class);
    final var graphRead =
      new AsUnmodifiableGraph<>(graph);

    final var nodes =
      new HashMap<UUID, OBCompositionNodeType>(1024);
    final var subscriptions =
      new HashMap<UUID, Disposable>(1024);
    final var nodesRead =
      Collections.unmodifiableMap(nodes);

    final var compositionGraph =
      new OBCompositionGraph(
        graphRead,
        graph,
        nodes,
        subscriptions,
        nodesRead,
        eventFactory,
        eventSubject,
        services,
        strings
      );

    final var root = new OBCompositionRoot(compositionGraph, strings, id);
    compositionGraph.setRoot(root);
    return compositionGraph;
  }

  private void setRoot(
    final OBCompositionRoot newRoot)
  {
    Preconditions.checkPreconditionV(
      this.root == null,
      "Root must not have been set"
    );

    this.root = Objects.requireNonNull(newRoot, "root");
    this.nodes.put(newRoot.id(), newRoot);
    this.graph.addVertex(newRoot);
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
    final UUID id,
    final OBNodeMetadata nodeMetadata,
    final OBChannelMetadata channelMetadata)
    throws OBException
  {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(nodeMetadata, "nodeMetadata");
    Objects.requireNonNull(channelMetadata, "channelMetadata");

    final var existing = this.nodes.get(id);
    if (this.nodes.containsKey(id)) {
      throw OBDuplicateException.objectDuplicate(
        this.strings,
        existing.type(),
        id.toString()
      );
    }

    final var channel = this.makeChannel(id, nodeMetadata, channelMetadata);
    this.announce(this.eventFactory.graphNodeAdded(this.root, channel));
    this.subscribe(channel);
    return channel;
  }

  private void subscribe(
    final OBCompositionNodeType node)
  {
    Preconditions.checkPreconditionV(
      !this.subscriptions.containsKey(node.id()),
      "Subscription cannot exist for %s",
      node.id()
    );

    final var nodeSub =
      node.changes().subscribe(ignored -> {
        this.eventSubject.onNext(
          OBGraphNodeModifiedEvent.builder()
            .setMessage(this.strings.nodeModified())
            .setNodeTarget(node)
            .build()
        );
      });

    this.subscriptions.put(node.id(), nodeSub);
  }

  @Override
  public OBChannelType createChannel(
    final OBNodeMetadata nodeMetadata,
    final OBChannelMetadata channelMetadata)
  {
    Objects.requireNonNull(nodeMetadata, "nodeMetadata");
    Objects.requireNonNull(channelMetadata, "channelMetadata");

    final var channel =
      this.makeChannel(this.createId(), nodeMetadata, channelMetadata);
    this.announce(this.eventFactory.graphNodeAdded(this.root, channel));
    this.subscribe(channel);
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
    this.unsubscribe(node);
    this.announce(this.eventFactory.graphNodeRemoved(edge.nodeSource(), node));
  }

  private void unsubscribe(
    final OBCompositionNodeType node)
  {
    Preconditions.checkPreconditionV(
      this.subscriptions.containsKey(node.id()),
      "Subscription must exist for %s",
      node.id()
    );

    final var subscription = this.subscriptions.remove(node.id());
    subscription.dispose();
  }

  @Override
  public boolean nodeIsDeleted(
    final OBCompositionNodeType node)
  {
    Objects.requireNonNull(node, "node");
    return !Objects.equals(this.nodes.get(node.id()), node);
  }

  private OBChannelType makeChannel(
    final UUID id,
    final OBNodeMetadata nodeMetadata,
    final OBChannelMetadata channelMetadata)
  {
    final var channel =
      OBChannel.create(
        this.services,
        this,
        id,
        nodeMetadata,
        channelMetadata
      );

    final var edge = OBCompositionEdge.of(this.root, channel);
    this.graph.addVertex(channel);
    this.graph.addEdge(this.root, channel, edge);
    this.nodes.put(id, channel);
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
  public <A, T extends OBRegionType<A>> T createRegion(
    final OBCompositionNodeType owner,
    final OBNodeMetadata nodeMetadata,
    final OBRegionConstructorType<A, T> constructor,
    final A regionData)
  {
    Objects.requireNonNull(owner, "owner");
    Objects.requireNonNull(nodeMetadata, "nodeMetadata");
    Objects.requireNonNull(constructor, "constructor");
    Objects.requireNonNull(regionData, "regionData");

    final T region =
      this.makeRegion(
        owner,
        this.createId(),
        nodeMetadata,
        constructor,
        regionData
      );
    this.announce(this.eventFactory.graphNodeAdded(owner, region));
    this.subscribe(region);
    return region;
  }

  @Override
  public <A, T extends OBRegionType<A>> T createRegion(
    final OBCompositionNodeType owner,
    final UUID id,
    final OBNodeMetadata nodeMetadata,
    final OBRegionConstructorType<A, T> constructor,
    final A regionData)
    throws OBException
  {
    Objects.requireNonNull(owner, "owner");
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(nodeMetadata, "nodeMetadata");
    Objects.requireNonNull(constructor, "constructor");
    Objects.requireNonNull(regionData, "regionData");

    final var existing = this.nodes.get(id);
    if (this.nodes.containsKey(id)) {
      throw OBDuplicateException.objectDuplicate(
        this.strings,
        existing.type(),
        id.toString()
      );
    }

    final var region =
      this.makeRegion(
        owner,
        id,
        nodeMetadata,
        constructor,
        regionData
      );
    this.announce(this.eventFactory.graphNodeAdded(owner, region));
    this.subscribe(region);
    return region;
  }

  private <A, T extends OBRegionType<A>> T makeRegion(
    final OBCompositionNodeType owner,
    final UUID id,
    final OBNodeMetadata nodeMetadata,
    final OBRegionConstructorType<A, T> constructor,
    final A regionData)
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

    final var region =
      constructor.construct(this.services, this, id, nodeMetadata, regionData);
    final var edge = OBCompositionEdge.of(owner, region);
    this.graph.addVertex(region);
    this.graph.addEdge(owner, region, edge);
    this.nodes.put(id, region);
    return region;
  }
}
