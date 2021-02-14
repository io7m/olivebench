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

package com.io7m.olivebench.composition.internal;

import com.io7m.jranges.RangeInclusiveL;
import com.io7m.olivebench.composition.OBClockServiceType;
import com.io7m.olivebench.composition.OBCompositionEventType;
import com.io7m.olivebench.composition.OBCompositionMetadata;
import com.io7m.olivebench.composition.OBCompositionModificationTimeChangedEvent;
import com.io7m.olivebench.composition.OBCompositionModifiedEvent;
import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.ports.OBPortInputType;
import com.io7m.olivebench.composition.ports.OBPortOutputType;
import com.io7m.olivebench.composition.ports.OBPortType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.io7m.olivebench.composition.OBCompositionChange.METADATA_CHANGED;
import static com.io7m.olivebench.composition.OBCompositionChange.PORT_CREATED;
import static com.io7m.olivebench.composition.OBCompositionChange.PORT_DELETED;
import static com.io7m.olivebench.composition.OBCompositionChange.PORT_UNDELETED;
import static com.io7m.olivebench.composition.OBCompositionChange.TRACK_CREATED;
import static com.io7m.olivebench.composition.OBCompositionChange.TRACK_DELETED;
import static com.io7m.olivebench.composition.OBCompositionChange.TRACK_UNDELETED;

public final class OBComposition implements OBCompositionType
{
  private final ConcurrentSkipListMap<UUID, OBPortType> ports;
  private final ConcurrentSkipListMap<UUID, OBTrackType> tracks;
  private final Disposable modifiedSubscription;
  private final OBClockServiceType clock;
  private final OBCompositionStrings strings;
  private final OBObjectMap objects;
  private final SortedMap<UUID, OBPortType> portsRead;
  private final SortedMap<UUID, OBTrackType> tracksRead;
  private final Subject<OBCompositionEventType> eventSubject;
  private volatile OBCompositionMetadata metadata;
  private volatile OffsetDateTime timeLastModified;

  public OBComposition(
    final OBClockServiceType inClock,
    final OBCompositionStrings inStrings,
    final OBCompositionMetadata inMetadata)
  {
    this.clock =
      Objects.requireNonNull(inClock, "clock");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.metadata =
      Objects.requireNonNull(inMetadata, "inMetadata");
    this.objects =
      new OBObjectMap(this.strings);

    this.tracks =
      new ConcurrentSkipListMap<>();
    this.tracksRead =
      Collections.unmodifiableSortedMap(this.tracks);

    this.ports =
      new ConcurrentSkipListMap<>();
    this.portsRead =
      Collections.unmodifiableSortedMap(this.ports);

    this.timeLastModified =
      this.clock.now();
    this.eventSubject =
      PublishSubject.<OBCompositionEventType>create()
        .toSerialized();
    this.modifiedSubscription =
      this.eventSubject.ofType(OBCompositionModifiedEvent.class)
        .subscribe(this::onModified);
  }

  private void onModified(
    final OBCompositionModifiedEvent event)
  {
    this.setLastModified(this.clock.now());
  }

  private OBTrackType trackSave(
    final OBTrack newTrack)
  {
    this.tracks.put(newTrack.id(), newTrack);
    this.publish(OBCompositionModifiedEvent.of(TRACK_CREATED, newTrack.id()));
    return newTrack;
  }

  boolean trackIsDeleted(
    final OBTrack track)
  {
    return !Objects.equals(this.tracks.get(track.id()), track);
  }

  OBTrackType trackDelete(
    final OBTrack track)
  {
    if (this.trackIsDeleted(track)) {
      throw new IllegalStateException(
        this.strings().format("trackAlreadyDeleted", track.id())
      );
    }

    this.tracks.remove(track.id());
    this.publish(OBCompositionModifiedEvent.of(TRACK_DELETED, track.id()));
    return track;
  }

  OBTrackType trackUndelete(
    final OBTrack track)
  {
    if (!this.trackIsDeleted(track)) {
      throw new IllegalStateException(
        this.strings().format("trackNotDeleted", track.id())
      );
    }

    this.tracks.put(track.id(), track);
    this.publish(OBCompositionModifiedEvent.of(TRACK_UNDELETED, track.id()));
    return track;
  }

  private <T extends OBPortType> T portSave(
    final T newPort)
  {
    this.ports.put(newPort.id(), newPort);
    this.publish(OBCompositionModifiedEvent.of(PORT_CREATED, newPort.id()));
    return newPort;
  }

  boolean portIsDeleted(
    final OBPortType port)
  {
    return !Objects.equals(this.ports.get(port.id()), port);
  }

  OBPortType portDelete(
    final OBPortType port)
  {
    if (this.portIsDeleted(port)) {
      throw new IllegalStateException(
        this.strings().format("portAlreadyDeleted", port.id())
      );
    }

    this.ports.remove(port.id());
    this.publish(OBCompositionModifiedEvent.of(PORT_DELETED, port.id()));
    return port;
  }

  OBPortType portUndelete(
    final OBPortType port)
  {
    if (!this.portIsDeleted(port)) {
      throw new IllegalStateException(
        this.strings().format("portNotDeleted", port.id())
      );
    }

    this.ports.put(port.id(), port);
    this.publish(OBCompositionModifiedEvent.of(PORT_UNDELETED, port.id()));
    return port;
  }

  OBObjectMap objects()
  {
    return this.objects;
  }

  OBCompositionStrings strings()
  {
    return this.strings;
  }

  @Override
  public Observable<OBCompositionEventType> events()
  {
    return this.eventSubject;
  }

  @Override
  public OBCompositionMetadata metadata()
  {
    return this.metadata;
  }

  @Override
  public void setMetadata(
    final OBCompositionMetadata newMetadata)
  {
    final var newId = newMetadata.id();
    final var oldId = this.metadata.id();
    if (!Objects.equals(newId, oldId)) {
      throw new IllegalArgumentException("Cannot change the composition ID");
    }

    this.metadata = Objects.requireNonNull(newMetadata, "newMetadata");
    this.publish(OBCompositionModifiedEvent.of(METADATA_CHANGED, newId));
  }

  @Override
  public SortedMap<UUID, OBTrackType> tracks()
  {
    return this.tracksRead;
  }

  @Override
  public OBTrackType createTrack()
  {
    return this.trackSave(
      this.objects.withFresh(freshId -> new OBTrack(this, freshId))
    );
  }

  @Override
  public OBTrackType createTrack(
    final UUID id)
  {
    return this.trackSave(
      this.objects.withSpecific(id, freshId -> new OBTrack(this, freshId))
    );
  }

  @Override
  public OffsetDateTime lastModified()
  {
    return this.timeLastModified;
  }

  @Override
  public void setLastModified(
    final OffsetDateTime time)
  {
    this.timeLastModified =
      Objects.requireNonNull(time, "time");
    this.eventSubject.onNext(
      OBCompositionModificationTimeChangedEvent.of(this.timeLastModified)
    );
  }

  @Override
  public RangeInclusiveL noteRange()
  {
    return RangeInclusiveL.of(0L, 127L);
  }

  @Override
  public SortedMap<UUID, OBPortType> ports()
  {
    return this.portsRead;
  }

  @Override
  public OBPortOutputType createOutputPort()
  {
    return this.portSave(
      this.objects.withFresh(freshId -> new OBPortOutput(this, freshId))
    );
  }

  @Override
  public OBPortOutputType createOutputPort(final UUID id)
  {
    return this.portSave(
      this.objects.withSpecific(id, freshId -> new OBPortOutput(this, freshId))
    );
  }

  @Override
  public OBPortInputType createInputPort()
  {
    return this.portSave(
      this.objects.withFresh(freshId -> new OBPortInput(this, freshId))
    );
  }

  @Override
  public OBPortInputType createInputPort(final UUID id)
  {
    return this.portSave(
      this.objects.withSpecific(id, freshId -> new OBPortInput(this, freshId))
    );
  }

  public void publish(
    final OBCompositionEventType event)
  {
    this.eventSubject.onNext(Objects.requireNonNull(event, "event"));
  }
}
