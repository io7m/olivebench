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
import com.io7m.olivebench.model.graph.OBGraphNodeRemovedEvent;
import com.io7m.olivebench.model.graph.OBRegionType;
import com.io7m.olivebench.model.metadata.OBMetadata;
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
  private final PublishSubject<OBCompositionEventType> eventSubject;
  private final OBStringsType strings;
  private volatile OBMetadata metadata;
  private volatile Optional<Path> fileName;

  private OBComposition(
    final OBStringsType inStrings,
    final OBCompositionGraphType inGraph,
    final OBMetadata inMetadata)
  {
    this.graph =
      Objects.requireNonNull(inGraph, "inGraph");
    this.metadata =
      Objects.requireNonNull(inMetadata, "inMetadata");
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");

    this.eventSubject =
      PublishSubject.create();

    this.graph.events().subscribe(
      this::onGraphEvent,
      this.eventSubject::onError,
      () -> {

      }
    );
  }

  public static OBCompositionType create(
    final OBStringsType strings)
  {
    return createWith(
      strings,
      OBCompositionGraph.createWith(strings, UUID.randomUUID())
    );
  }

  public static OBCompositionType createWith(
    final OBStringsType strings,
    final OBCompositionGraphType graph)
  {
    return new OBComposition(
      strings,
      graph,
      OBMetadata.of(List.of())
    );
  }

  private void onGraphEvent(
    final OBGraphEventType event)
  {
    if (event instanceof OBGraphNodeAddedEvent) {
      this.onGraphEventAdded((OBGraphNodeAddedEvent) event);
    } else if (event instanceof OBGraphNodeRemovedEvent) {
      this.onGraphEventRemoved((OBGraphNodeRemovedEvent) event);
    } else {
      throw new UnreachableCodeException();
    }
  }

  private void onGraphEventRemoved(
    final OBGraphNodeRemovedEvent removedEvent)
  {
    final var removedNode = removedEvent.node();
    if (removedNode instanceof OBChannelType) {
      this.eventSubject.onNext(
        OBChannelEventRemoved.builder()
          .setMessage(this.strings.channelDeleted())
          .putAttributes(this.strings.channel(), removedNode.name().value())
          .setChannel((OBChannelType) removedNode)
          .build()
      );
    } else if (removedNode instanceof OBRegionType) {
      this.eventSubject.onNext(
        OBRegionEventRemoved.builder()
          .setMessage(this.strings.regionDeleted())
          .putAttributes(this.strings.channel(), removedNode.name().value())
          .setChannel((OBChannelType) removedEvent.parent())
          .setRegion((OBRegionType) removedNode)
          .build()
      );
    } else {
      throw new UnreachableCodeException();
    }
  }

  private void onGraphEventAdded(
    final OBGraphNodeAddedEvent graphEvent)
  {
    final var addedNode = graphEvent.nodeTarget();
    if (addedNode instanceof OBChannelType) {
      this.eventSubject.onNext(
        OBChannelEventAdded.builder()
          .setMessage(this.strings.channelCreated())
          .putAttributes(this.strings.channel(), addedNode.name().value())
          .setChannel((OBChannelType) addedNode)
          .build()
      );
    } else if (addedNode instanceof OBRegionType) {
      this.eventSubject.onNext(
        OBRegionEventAdded.builder()
          .setMessage(this.strings.regionCreated())
          .putAttributes(this.strings.channel(), addedNode.name().value())
          .setChannel((OBChannelType) graphEvent.nodeSource())
          .setRegion((OBRegionType) addedNode)
          .build()
      );
    } else {
      throw new UnreachableCodeException();
    }
  }

  @Override
  public OBMetadata metadata()
  {
    return this.metadata;
  }

  @Override
  public OBCompositionGraphType graph()
  {
    return this.graph;
  }

  @Override
  public void setMetadata(
    final OBMetadata inMetadata)
  {
    this.metadata = Objects.requireNonNull(inMetadata, "Metadata");
    this.eventSubject.onNext(
      OBMetadataChangedEvent.builder()
        .setMessage(this.strings.metadataChanged())
        .setMetadata(inMetadata)
        .build()
    );
  }

  @Override
  public void setFileName(final Path file)
  {
    this.fileName = Optional.of(file);
  }

  @Override
  public OBCompositionReadableType snapshot()
  {
    return new OBCompositionSnapshot(
      this.graph.snapshot(),
      this.metadata
    );
  }

  @Override
  public Observable<OBCompositionEventType> events()
  {
    return this.eventSubject;
  }

  @Override
  public Optional<Path> fileName()
  {
    return this.fileName;
  }
}
