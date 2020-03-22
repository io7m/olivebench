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

import checkers.units.quals.A;
import com.io7m.jaffirm.core.PreconditionViolationException;
import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.exceptions.OBDuplicateException;
import com.io7m.olivebench.model.OBCompositionEventType;
import com.io7m.olivebench.model.graph.OBCompositionGraph;
import com.io7m.olivebench.model.graph.OBGraphNodeAddedEvent;
import com.io7m.olivebench.model.graph.OBGraphNodeRemovedEvent;
import com.io7m.olivebench.model.graph.OBTextRegion;
import com.io7m.olivebench.model.names.OBName;
import com.io7m.olivebench.model.spaces.OBSpaceRegionType;
import com.io7m.olivebench.strings.OBStrings;
import com.io7m.olivebench.strings.OBStringsType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

public final class OBCompositionGraphTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBCompositionGraphTest.class);

  private OBStringsType strings;
  private ArrayList<OBCompositionEventType> events;

  @BeforeEach
  public void testSetup()
  {
    this.strings = OBStrings.of(OBStrings.getResourceBundle());
    this.events = new ArrayList<>();
  }

  @Test
  public void testCreate()
  {
    final var id = UUID.randomUUID();
    final var composition = OBCompositionGraph.createWith(this.strings, id);
    Assertions.assertEquals(id, composition.id());
  }

  @Test
  public void testCreateChannel()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 =
      composition.createChannel("channel0");
    final var channel1 =
      composition.createChannel("channel1");
    final var channel2 =
      composition.createChannel("channel1");
    final var channel3 =
      composition.createChannel(UUID.randomUUID(), "channel3");

    Assertions.assertEquals("channel0", channel0.name().value());
    Assertions.assertEquals("channel1", channel1.name().value());
    Assertions.assertEquals("channel3", channel3.name().value());
    Assertions.assertEquals(channel1.name(), channel2.name());
    Assertions.assertNotEquals(channel0.id(), channel1.id());
    Assertions.assertNotEquals(channel0.id(), channel2.id());
    Assertions.assertNotEquals(channel0.id(), channel3.id());
    Assertions.assertNotEquals(channel1.id(), channel2.id());
    Assertions.assertNotEquals(channel1.id(), channel3.id());

    {
      final OBGraphNodeAddedEvent event =
        (OBGraphNodeAddedEvent) this.events.remove(0);
      Assertions.assertEquals(
        channel0.id().toString(),
        event.attributes().get("Target"));
    }
    {
      final OBGraphNodeAddedEvent event =
        (OBGraphNodeAddedEvent) this.events.remove(0);
      Assertions.assertEquals(
        channel1.id().toString(),
        event.attributes().get("Target"));
    }
    {
      final OBGraphNodeAddedEvent event =
        (OBGraphNodeAddedEvent) this.events.remove(0);
      Assertions.assertEquals(
        channel2.id().toString(),
        event.attributes().get("Target"));
    }
    {
      final OBGraphNodeAddedEvent event =
        (OBGraphNodeAddedEvent) this.events.remove(0);
      Assertions.assertEquals(
        channel3.id().toString(),
        event.attributes().get("Target"));
    }
    Assertions.assertEquals(0, this.events.size());
  }

  @Test
  public void testCreateChannelSetGet()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 = composition.createChannel("channel0");
    channel0.setName("xyz");
    Assertions.assertEquals("xyz", channel0.name().value());
  }

  @Test
  public void testCreateChannelDuplicate()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 = composition.createChannel("channel0");
    Assertions.assertThrows(OBDuplicateException.class, () -> {
      composition.createChannel(channel0.id(), "channel1");
    });
  }

  @Test
  public void testCreateDeleteChannel()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 = composition.createChannel("channel0");
    Assertions.assertFalse(channel0.isDeleted());
    composition.nodeDelete(channel0);
    Assertions.assertTrue(channel0.isDeleted());
  }

  @Test
  public void testCreateDeleteDeleteChannel()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 = composition.createChannel("channel0");
    Assertions.assertFalse(channel0.isDeleted());
    composition.nodeDelete(channel0);
    Assertions.assertTrue(channel0.isDeleted());

    Assertions.assertThrows(PreconditionViolationException.class, () -> {
      composition.nodeDelete(channel0);
    });
  }

  @Test
  public void testCreateDeleteRegion()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 = composition.createChannel("channel0");
    final var region0 =
      composition.createRegion(
        channel0,
        PAreaL.of(0L, 0L, 0L, 0L),
        (graph, id, area) -> OBTextRegion.create(
          graph,
          this.strings,
          id,
          area));

    Assertions.assertFalse(region0.isDeleted());
    composition.nodeDelete(region0);
    Assertions.assertTrue(region0.isDeleted());

    final var region1 =
      composition.createRegion(
        channel0,
        region0.id(),
        PAreaL.of(0L, 0L, 0L, 0L),
        (graph, id, area) -> OBTextRegion.create(
          graph,
          this.strings,
          id,
          area));

    Assertions.assertFalse(region1.isDeleted());
    composition.nodeDelete(region1);
    Assertions.assertTrue(region1.isDeleted());

    {
      final OBGraphNodeAddedEvent event =
        (OBGraphNodeAddedEvent) this.events.remove(0);
      Assertions.assertEquals(
        "Channel",
        event.attributes().get("Target Type"));
    }
    {
      final OBGraphNodeAddedEvent event =
        (OBGraphNodeAddedEvent) this.events.remove(0);
      Assertions.assertEquals(
        "Region",
        event.attributes().get("Target Type"));
    }
    {
      final OBGraphNodeRemovedEvent event =
        (OBGraphNodeRemovedEvent) this.events.remove(0);
      Assertions.assertEquals(
        "Region",
        event.attributes().get("Target Type"));
    }
    {
      final OBGraphNodeAddedEvent event =
        (OBGraphNodeAddedEvent) this.events.remove(0);
      Assertions.assertEquals(
        "Region",
        event.attributes().get("Target Type"));
    }
    {
      final OBGraphNodeRemovedEvent event =
        (OBGraphNodeRemovedEvent) this.events.remove(0);
      Assertions.assertEquals(
        "Region",
        event.attributes().get("Target Type"));
    }
    Assertions.assertEquals(0, this.events.size());
  }

  @Test
  public void testCreateRegionConflict0()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 =
      composition.createChannel("channel0");

    Assertions.assertThrows(OBDuplicateException.class, () -> {
      composition.createRegion(
        channel0,
        channel0.id(),
        PAreaL.of(0L, 0L, 0L, 0L),
        (graph, id, area) ->
          OBTextRegion.create(graph, this.strings, id, area));
    });
  }

  @Test
  public void testCreateRegionConflict1()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 =
      composition.createChannel("channel0");

    final var region0 =
      composition.createRegion(
        channel0,
        PAreaL.of(0L, 0L, 0L, 0L),
        (graph, id, area) -> OBTextRegion.create(
          graph,
          this.strings,
          id,
          area));

    Assertions.assertThrows(OBDuplicateException.class, () -> {
      composition.createRegion(
        channel0,
        region0.id(),
        PAreaL.of(0L, 0L, 0L, 0L),
        (graph, id, area) -> OBTextRegion.create(
          graph,
          this.strings,
          id,
          area));
    });
  }

  @Test
  public void testCreateChannelConflict0()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 =
      composition.createChannel("channel0");

    final var region0 =
      composition.createRegion(
        channel0,
        PAreaL.of(0L, 0L, 0L, 0L),
        (graph, id, area) -> OBTextRegion.create(
          graph,
          this.strings,
          id,
          area));

    Assertions.assertThrows(OBDuplicateException.class, () -> {
      composition.createChannel(region0.id(), "channel0");
    });
  }

  @Test
  public void testCreateChannelConflict1()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 =
      composition.createChannel("channel0");

    Assertions.assertThrows(OBDuplicateException.class, () -> {
      composition.createChannel(channel0.id(), "channel0");
    });
  }

  @Test
  public void testCreateTextRegionSetGet()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 = composition.createChannel("channel0");
    final var region0 =
      composition.createRegion(
        channel0,
        PAreaL.of(0L, 0L, 0L, 0L),
        (graph, id, area) -> OBTextRegion.create(
          graph,
          this.strings,
          id,
          area));

    Assertions.assertEquals("", region0.text());
    region0.setText("xyz");
    Assertions.assertEquals("xyz", region0.text());
  }

  @Test
  public void testSnapshot()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var channel0 = composition.createChannel("channel0");
    composition.createRegion(
      channel0,
      PAreaL.of(0L, 0L, 0L, 0L),
      (graph, id, area) -> OBTextRegion.create(
        graph,
        this.strings,
        id,
        area));

    composition.createRegion(
      channel0,
      PAreaL.of(0L, 0L, 0L, 0L),
      (graph, id, area) -> OBTextRegion.create(
        graph,
        this.strings,
        id,
        area));

    composition.createRegion(
      channel0,
      PAreaL.of(0L, 0L, 0L, 0L),
      (graph, id, area) -> OBTextRegion.create(
        graph,
        this.strings,
        id,
        area));

    final var snap = composition.snapshot();
    Assertions.assertEquals(composition.nodes(), snap.nodes());
    Assertions.assertEquals(composition.graph(), snap.graph());
    Assertions.assertEquals(composition.id(), snap.id());
    Assertions.assertEquals(composition.type(), snap.type());
    Assertions.assertEquals(composition.root(), snap.root());
    Assertions.assertTrue(snap.events().isEmpty().blockingGet().booleanValue());
    Assertions.assertEquals(snap, snap.snapshot());
  }

  @Test
  public void testCreateChannelSetName()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var names = new ArrayList<OBName>();
    final var channel = composition.createChannel("channel0");
    channel.nameProperty().subscribe(names::add);
    channel.setName("a");
    channel.setName("b");
    channel.setName("c");

    Assertions.assertEquals(3, names.size());
    Assertions.assertEquals("a", names.get(0).value());
    Assertions.assertEquals("b", names.get(1).value());
    Assertions.assertEquals("c", names.get(2).value());
  }

  @Test
  public void testCreateChannelSetArea()
    throws Exception
  {
    final var composition = OBCompositionGraph.create(this.strings);
    composition.events().subscribe(this::logEvent);

    final var areas = new ArrayList<PAreaL<OBSpaceRegionType>>();
    final var channel = composition.createChannel("channel0");
    channel.areaRelativeProperty().subscribe(areas::add);
    channel.setAreaRelative(PAreaL.of(0L, 0L, 1L, 1L));
    channel.setAreaRelative(PAreaL.of(0L, 0L, 2L, 2L));
    channel.setAreaRelative(PAreaL.of(0L, 0L, 3L, 3L));

    Assertions.assertEquals(3, areas.size());
    Assertions.assertEquals(
      PAreaL.of(0L, 0L, 1L, 1L),
      areas.get(0));
    Assertions.assertEquals(
      PAreaL.of(0L, 0L, 2L, 2L),
      areas.get(1));
    Assertions.assertEquals(
      PAreaL.of(0L, 0L, 3L, 3L),
      areas.get(2));
  }

  private void logEvent(
    final OBCompositionEventType event)
  {
    LOG.debug("event: {}", event);
    this.events.add(event);
  }
}
