/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.olivebench.tests.composition;

import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.composition.OBCompositionChange;
import com.io7m.olivebench.composition.OBCompositionEventType;
import com.io7m.olivebench.composition.OBCompositionMetadata;
import com.io7m.olivebench.composition.OBCompositionModifiedEvent;
import com.io7m.olivebench.composition.OBCompositions;
import com.io7m.olivebench.composition.OBTimeConfiguration;
import com.io7m.olivebench.events.api.OBEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import static com.io7m.olivebench.composition.OBCompositionChange.REGION_CREATED;
import static com.io7m.olivebench.composition.OBCompositionChange.REGION_DELETED;
import static com.io7m.olivebench.composition.OBCompositionChange.REGION_UNDELETED;
import static com.io7m.olivebench.composition.OBCompositionChange.TRACK_CREATED;
import static com.io7m.olivebench.composition.OBCompositionChange.TRACK_DELETED;
import static com.io7m.olivebench.composition.OBCompositionChange.TRACK_UNDELETED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class OBCompositionTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBCompositionTest.class);

  private OBCompositions compositions;
  private OBTimeConfiguration timeConfiguration;
  private ArrayList<OBEventType> events;

  private void logEvent(
    final OBCompositionEventType event)
  {
    LOG.debug("event: {}", event);
    this.events.add(event);
  }

  private void eventIs(
    final OBCompositionChange change)
  {
    final var event = (OBCompositionModifiedEvent) this.events.remove(0);
    assertEquals(change, event.change());
  }

  @BeforeEach
  public void setup()
  {
    this.compositions = new OBCompositions();
    this.timeConfiguration =
      OBTimeConfiguration.builder()
        .setTicksPerQuarterNote(1024L)
        .build();

    this.events = new ArrayList<OBEventType>();
  }

  @Test
  public void testEmpty()
  {
    final var metadata =
      OBCompositionMetadata.builder()
        .setId(UUID.randomUUID())
        .setTimeConfiguration(this.timeConfiguration)
        .build();

    final var composition =
      this.compositions.createComposition(Locale.getDefault(), metadata);
    composition.events().subscribe(this::logEvent);

    assertEquals(metadata, composition.metadata());
    assertEquals(0, composition.tracks().size());
    assertEquals(0, this.events.size());
  }

  @Test
  public void testCreateTrack()
  {
    final var metadata =
      OBCompositionMetadata.builder()
        .setId(UUID.randomUUID())
        .setTimeConfiguration(this.timeConfiguration)
        .build();

    final var composition =
      this.compositions.createComposition(Locale.getDefault(), metadata);
    composition.events().subscribe(this::logEvent);

    final var track = composition.createTrack();
    assertEquals(1, composition.tracks().size());
    assertFalse(track.isDeleted());
    track.delete();
    assertTrue(track.isDeleted());

    assertEquals(metadata, composition.metadata());
    assertEquals(0, composition.tracks().size());
    assertEquals(2, this.events.size());

    this.eventIs(TRACK_CREATED);
    this.eventIs(TRACK_DELETED);

    assertEquals(0, this.events.size());
    assertThrows(IllegalStateException.class, track::delete);
    assertEquals(0, this.events.size());
  }

  @Test
  public void testCreateUndeleteTrack()
  {
    final var metadata =
      OBCompositionMetadata.builder()
        .setId(UUID.randomUUID())
        .setTimeConfiguration(this.timeConfiguration)
        .build();

    final var composition =
      this.compositions.createComposition(Locale.getDefault(), metadata);
    composition.events().subscribe(this::logEvent);

    final var track = composition.createTrack();
    assertFalse(track.isDeleted());
    track.delete();
    assertTrue(track.isDeleted());
    track.undelete();
    assertFalse(track.isDeleted());

    assertEquals(metadata, composition.metadata());
    assertEquals(1, composition.tracks().size());
    assertEquals(3, this.events.size());

    this.eventIs(TRACK_CREATED);
    this.eventIs(TRACK_DELETED);
    this.eventIs(TRACK_UNDELETED);

    assertEquals(0, this.events.size());
    assertThrows(IllegalStateException.class, track::undelete);
    assertEquals(0, this.events.size());
  }

  @Test
  public void testCreateTrackIDUsed()
  {
    final var metadata =
      OBCompositionMetadata.builder()
        .setId(UUID.randomUUID())
        .setTimeConfiguration(this.timeConfiguration)
        .build();

    final var composition =
      this.compositions.createComposition(Locale.getDefault(), metadata);
    composition.events().subscribe(this::logEvent);

    final var id = UUID.randomUUID();
    composition.createTrack(id);

    assertThrows(IllegalArgumentException.class, () -> {
      composition.createTrack(id);
    });
  }

  @Test
  public void testCreateRegion()
  {
    final var metadata =
      OBCompositionMetadata.builder()
        .setId(UUID.randomUUID())
        .setTimeConfiguration(this.timeConfiguration)
        .build();

    final var composition =
      this.compositions.createComposition(Locale.getDefault(), metadata);
    composition.events().subscribe(this::logEvent);

    final var track =
      composition.createTrack();

    final var r0 =
      track.createTextRegion(PAreaL.of(0L, 1L, 0L, 1L));
    assertFalse(r0.isDeleted());
    final var r1 =
      track.createTextRegion(PAreaL.of(0L, 1L, 0L, 1L));
    assertFalse(r1.isDeleted());
    final var r2 =
      track.createTextRegion(PAreaL.of(0L, 1L, 0L, 1L));
    assertFalse(r2.isDeleted());

    assertEquals(3, track.regions().size());

    r2.delete();
    assertTrue(r2.isDeleted());
    r1.delete();
    assertTrue(r1.isDeleted());
    r0.delete();
    assertTrue(r0.isDeleted());

    assertEquals(0, track.regions().size());
    assertEquals(metadata, composition.metadata());
    assertEquals(1, composition.tracks().size());
    assertEquals(7, this.events.size());

    this.eventIs(TRACK_CREATED);
    this.eventIs(REGION_CREATED);
    this.eventIs(REGION_CREATED);
    this.eventIs(REGION_CREATED);
    this.eventIs(REGION_DELETED);
    this.eventIs(REGION_DELETED);
    this.eventIs(REGION_DELETED);

    assertEquals(0, this.events.size());
    assertThrows(IllegalStateException.class, r2::delete);
    assertThrows(IllegalStateException.class, r1::delete);
    assertThrows(IllegalStateException.class, r0::delete);
    assertEquals(0, this.events.size());
  }

  @Test
  public void testCreateUndeleteRegion()
  {
    final var metadata =
      OBCompositionMetadata.builder()
        .setId(UUID.randomUUID())
        .setTimeConfiguration(this.timeConfiguration)
        .build();

    final var composition =
      this.compositions.createComposition(Locale.getDefault(), metadata);
    composition.events().subscribe(this::logEvent);

    final var track =
      composition.createTrack();

    final var r0 =
      track.createTextRegion(PAreaL.of(0L, 1L, 0L, 1L));
    r0.delete();
    assertTrue(r0.isDeleted());
    r0.undelete();
    assertFalse(r0.isDeleted());

    this.eventIs(TRACK_CREATED);
    this.eventIs(REGION_CREATED);
    this.eventIs(REGION_DELETED);
    this.eventIs(REGION_UNDELETED);

    assertEquals(0, this.events.size());
    assertThrows(IllegalStateException.class, r0::undelete);
    assertEquals(0, this.events.size());
  }
}
