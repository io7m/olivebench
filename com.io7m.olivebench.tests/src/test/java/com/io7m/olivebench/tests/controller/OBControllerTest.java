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

package com.io7m.olivebench.tests.controller;

import com.io7m.olivebench.composition.OBCompositionFactoryType;
import com.io7m.olivebench.composition.OBCompositions;
import com.io7m.olivebench.composition.parser.api.OBCompositionParseException;
import com.io7m.olivebench.composition.parser.api.OBCompositionParsers;
import com.io7m.olivebench.composition.parser.api.OBCompositionParsersType;
import com.io7m.olivebench.composition.parser.spi.OBCompositionSPIParsersType;
import com.io7m.olivebench.composition.serializer.spi.OBCompositionSPISerializersType;
import com.io7m.olivebench.controller.OBController;
import com.io7m.olivebench.controller.api.OBControllerCommandEvent;
import com.io7m.olivebench.controller.api.OBControllerCommandEventKind;
import com.io7m.olivebench.controller.api.OBControllerCommandFailedEvent;
import com.io7m.olivebench.controller.api.OBControllerCompositionEvent;
import com.io7m.olivebench.controller.api.OBControllerCompositionEventKind;
import com.io7m.olivebench.controller.api.OBControllerType;
import com.io7m.olivebench.events.api.OBEventType;
import com.io7m.olivebench.services.api.OBServiceDirectory;
import com.io7m.olivebench.tests.OBFileAndStream;
import com.io7m.olivebench.tests.OBTestDirectories;
import com.io7m.olivebench.xml.v1.OBCompositionParserV1;
import com.io7m.olivebench.xml.v1.OBCompositionSerializerV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Locale;

import static com.io7m.olivebench.controller.api.OBControllerCommandEventKind.*;
import static com.io7m.olivebench.controller.api.OBControllerCompositionEventKind.COMPOSITION_CLOSED;
import static com.io7m.olivebench.controller.api.OBControllerCompositionEventKind.COMPOSITION_OPENED;
import static com.io7m.olivebench.controller.api.OBControllerCompositionEventKind.COMPOSITION_UNDO_CHANGED;
import static com.io7m.olivebench.tests.OBTestDirectories.resourceStreamOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class OBControllerTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBControllerTest.class);

  private OBServiceDirectory services;
  private OBControllerType controller;
  private Path directory;
  private ArrayList<OBEventType> events;

  private void logEvent(
    final OBEventType event)
  {
    LOG.debug("event: {}", event);
    this.events.add(event);
  }

  private void eventIs(
    final OBControllerCompositionEventKind kind)
  {
    final var event = (OBControllerCompositionEvent) this.events.remove(0);
    assertEquals(kind, event.kind());
  }

  private void eventIs(
    final OBControllerCommandEventKind kind)
  {
    final var event = (OBControllerCommandEvent) this.events.remove(0);
    assertEquals(kind, event.kind());
  }

  private <T extends Exception> T failedIs(
    final Class<T> exceptionClass)
  {
    final var event = this.events.remove(0);
    if (event instanceof OBControllerCommandFailedEvent) {
      final var exception =
        ((OBControllerCommandFailedEvent) event).exception().get().getCause();
      assertEquals(exceptionClass, exception.getClass());
      return exceptionClass.cast(exception);
    }

    throw new IllegalStateException("Event is " + event);
  }

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.services = new OBServiceDirectory();
    this.services.register(
      OBCompositionFactoryType.class, new OBCompositions());
    this.services.register(
      OBCompositionSPIParsersType.class, new OBCompositionParserV1());
    this.services.register(
      OBCompositionSPISerializersType.class, new OBCompositionSerializerV1());
    this.services.register(
      OBCompositionParsersType.class, new OBCompositionParsers());

    this.controller =
      OBController.create(Clock.systemUTC(), this.services, Locale.ENGLISH);
    this.directory =
      OBTestDirectories.createTempDirectory();
    this.events =
      new ArrayList<OBEventType>();
    this.controller.events()
      .subscribe(this::logEvent);
  }

  @Test
  public void testLoadCloseComposition()
    throws IOException
  {
    final var stream =
      this.resource("composition-empty.xml");

    this.controller.compositionOpen(stream.path());
    assertFalse(this.controller.canUndo());
    assertFalse(this.controller.canRedo());
    this.controller.compositionClose();
    assertFalse(this.controller.canUndo());
    assertFalse(this.controller.canRedo());

    this.eventIs(COMMAND_STARTED);
    this.eventIs(COMPOSITION_OPENED);
    this.eventIs(COMPOSITION_UNDO_CHANGED);
    this.eventIs(COMMAND_ENDED);
    this.eventIs(COMMAND_STARTED);
    this.eventIs(COMPOSITION_CLOSED);
    this.eventIs(COMPOSITION_UNDO_CHANGED);
    this.eventIs(COMMAND_ENDED);
    assertEquals(0, this.events.size());
  }

  @Test
  public void testLoadCompositionFailed()
    throws IOException
  {
    final var stream =
      this.resource("composition-broken.xml");

    this.controller.compositionOpen(stream.path());
    assertFalse(this.controller.canUndo());
    assertFalse(this.controller.canRedo());
    this.controller.compositionClose();
    assertFalse(this.controller.canUndo());
    assertFalse(this.controller.canRedo());

    this.eventIs(COMMAND_STARTED);
    this.failedIs(OBCompositionParseException.class);
    this.eventIs(COMMAND_ENDED);
    this.eventIs(COMMAND_STARTED);
    this.eventIs(COMPOSITION_UNDO_CHANGED);
    this.eventIs(COMMAND_ENDED);
    assertEquals(0, this.events.size());
  }

  @Test
  public void testNewComposition()
    throws IOException
  {
    this.controller.compositionNew();
    assertFalse(this.controller.canUndo());
    assertFalse(this.controller.canRedo());
    this.controller.compositionClose();
    assertFalse(this.controller.canUndo());
    assertFalse(this.controller.canRedo());

    this.eventIs(COMMAND_STARTED);
    this.eventIs(COMPOSITION_OPENED);
    this.eventIs(COMPOSITION_UNDO_CHANGED);
    this.eventIs(COMMAND_ENDED);
    this.eventIs(COMMAND_STARTED);
    this.eventIs(COMPOSITION_CLOSED);
    this.eventIs(COMPOSITION_UNDO_CHANGED);
    this.eventIs(COMMAND_ENDED);
    assertEquals(0, this.events.size());
  }

  private OBFileAndStream resource(
    final String name)
    throws IOException
  {
    return resourceStreamOf(
      OBControllerTest.class,
      this.directory,
      name
    );
  }
}
