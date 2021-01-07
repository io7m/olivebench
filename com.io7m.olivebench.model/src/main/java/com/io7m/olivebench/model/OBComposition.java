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

package com.io7m.olivebench.model;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.olivebench.model.graph.OBChannelType;
import com.io7m.olivebench.model.graph.OBCompositionGraph;
import com.io7m.olivebench.model.graph.OBCompositionGraphType;
import com.io7m.olivebench.model.graph.OBGraphEventType;
import com.io7m.olivebench.model.graph.OBGraphNodeAddedEvent;
import com.io7m.olivebench.model.graph.OBGraphNodeModifiedEvent;
import com.io7m.olivebench.model.graph.OBGraphNodeRemovedEvent;
import com.io7m.olivebench.model.graph.OBRegionType;
import com.io7m.olivebench.model.metadata.OBCompositionMetadata;
import com.io7m.olivebench.model.properties.OBProperty;
import com.io7m.olivebench.model.properties.OBPropertyType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.strings.OBStringsType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class OBComposition implements OBCompositionType
{
  private final OBCompositionGraphType graph;
  private final OBPropertyType<OBCompositionMetadata> metadata;
  private final OBPropertyType<Optional<Path>> fileName;
  private final OBServiceDirectoryType services;
  private final OBStringsType strings;
  private final PublishSubject<OBCompositionEventType> eventSubject;

  private OBComposition(
    final OBServiceDirectoryType inServices,
    final OBStringsType inStrings,
    final OBCompositionGraphType inGraph,
    final OBCompositionMetadata inMetadata)
  {
    this.graph =
      Objects.requireNonNull(inGraph, "inGraph");
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
    this.metadata =
      OBProperty.create(Objects.requireNonNull(inMetadata, "inMetadata"));
    this.services =
      Objects.requireNonNull(inServices, "inServices");

    this.fileName =
      OBProperty.create(Optional.empty());

    this.eventSubject = PublishSubject.create();

    this.graph.events().subscribe(
      this::onGraphEvent,
      this.eventSubject::onError,
      () -> {

      }
    );

    this.metadata.asObservable().subscribe(
      this::onMetadataEvent,
      this.eventSubject::onError,
      () -> {

      }
    );
  }

  private void onMetadataEvent(
    final OBCompositionMetadata metadata)
  {
    this.eventSubject.onNext(
      OBCompositionMetadataChangedEvent.builder()
        .setMessage(this.strings.metadataChanged())
        .setMetadata(metadata)
        .build()
    );
  }

  public static OBCompositionType create(
    final OBServiceDirectoryType services)
  {
    return createWith(
      services,
      OBCompositionGraph.createWith(services, UUID.randomUUID())
    );
  }

  public static OBCompositionType createWith(
    final OBServiceDirectoryType services,
    final OBCompositionGraphType graph)
  {
    return new OBComposition(
      services,
      services.requireService(OBStringsType.class),
      graph,
      OBCompositionMetadata.of(List.of())
    );
  }

  private void onGraphEvent(
    final OBGraphEventType event)
  {
    if (event instanceof OBGraphNodeAddedEvent) {
      this.onGraphEventAdded((OBGraphNodeAddedEvent) event);
    } else if (event instanceof OBGraphNodeRemovedEvent) {
      this.onGraphEventRemoved((OBGraphNodeRemovedEvent) event);
    } else if (event instanceof OBGraphNodeModifiedEvent) {
      this.onGraphEventModified((OBGraphNodeModifiedEvent) event);
    } else {
      throw new UnreachableCodeException();
    }
  }

  private void onGraphEventModified(
    final OBGraphNodeModifiedEvent event)
  {
    final var modifiedNode = event.nodeTarget();
    final var name = modifiedNode.nodeMetadata().read().name().value();

    if (modifiedNode instanceof OBChannelType) {
      this.eventSubject.onNext(
        OBChannelEventChanged.builder()
          .setMessage(this.strings.channelChanged())
          .putAttributes(this.strings.channel(), name)
          .setChannel((OBChannelType) modifiedNode)
          .build()
      );
    } else if (modifiedNode instanceof OBRegionType) {
      this.eventSubject.onNext(
        OBRegionEventChanged.builder()
          .setMessage(this.strings.regionDeleted())
          .putAttributes(this.strings.channel(), name)
          .setChannel(this.graph.channelOf(modifiedNode))
          .setRegion((OBRegionType<?>) modifiedNode)
          .build()
      );
    } else {
      throw new UnreachableCodeException();
    }
  }

  private void onGraphEventRemoved(
    final OBGraphNodeRemovedEvent removedEvent)
  {
    final var removedNode = removedEvent.node();
    final var name = removedNode.nodeMetadata().read().name().value();

    if (removedNode instanceof OBChannelType) {
      this.eventSubject.onNext(
        OBChannelEventRemoved.builder()
          .setMessage(this.strings.channelDeleted())
          .putAttributes(this.strings.channel(), name)
          .setChannel((OBChannelType) removedNode)
          .build()
      );
      return;
    }

    if (removedNode instanceof OBRegionType) {
      this.eventSubject.onNext(
        OBRegionEventRemoved.builder()
          .setMessage(this.strings.regionDeleted())
          .putAttributes(this.strings.channel(), name)
          .setChannel(this.graph.channelOf(removedNode))
          .setRegion((OBRegionType<?>) removedNode)
          .build()
      );
      return;
    }

    throw new UnreachableCodeException();
  }

  private void onGraphEventAdded(
    final OBGraphNodeAddedEvent graphEvent)
  {
    final var addedNode = graphEvent.nodeTarget();
    final var name = addedNode.nodeMetadata().read().name().value();

    if (addedNode instanceof OBChannelType) {
      this.eventSubject.onNext(
        OBChannelEventAdded.builder()
          .setMessage(this.strings.channelCreated())
          .putAttributes(this.strings.channel(), name)
          .setChannel((OBChannelType) addedNode)
          .build()
      );
      return;
    }

    if (addedNode instanceof OBRegionType) {
      this.eventSubject.onNext(
        OBRegionEventAdded.builder()
          .setMessage(this.strings.regionCreated())
          .putAttributes(this.strings.channel(), name)
          .setChannel(this.graph.channelOf(addedNode))
          .setRegion((OBRegionType<?>) addedNode)
          .build()
      );
      return;
    }

    throw new UnreachableCodeException();
  }

  @Override
  public OBCompositionGraphType graph()
  {
    return this.graph;
  }

  @Override
  public OBPropertyType<OBCompositionMetadata> metadata()
  {
    return this.metadata;
  }

  @Override
  public OBPropertyType<Optional<Path>> fileName()
  {
    return this.fileName;
  }

  @Override
  public OBCompositionReadableType snapshot()
  {
    return new OBCompositionSnapshot(
      this.graph.snapshot(),
      this.metadata.read()
    );
  }

  @Override
  public Observable<OBCompositionEventType> events()
  {
    return this.eventSubject;
  }
}
