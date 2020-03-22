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

import com.io7m.olivebench.composition_parser.api.OBCompositionParsers;
import com.io7m.olivebench.composition_parser.api.OBCompositionParsersType;
import com.io7m.olivebench.composition_serializer.api.OBCompositionSerializers;
import com.io7m.olivebench.composition_serializer.api.OBCompositionSerializersType;
import com.io7m.olivebench.controller.OBController;
import com.io7m.olivebench.controller.OBControllerEventCompositionChanged;
import com.io7m.olivebench.controller.OBControllerEventCompositionStatusChanged;
import com.io7m.olivebench.controller.OBControllerEventTaskFailed;
import com.io7m.olivebench.controller.OBControllerEventTaskFinished;
import com.io7m.olivebench.controller.OBControllerEventTaskProgressChanged;
import com.io7m.olivebench.controller.OBControllerEventTaskStarted;
import com.io7m.olivebench.controller.OBControllerEventTaskUndoStatusChanged;
import com.io7m.olivebench.controller.OBControllerEventType;
import com.io7m.olivebench.model.names.OBName;
import com.io7m.olivebench.preferences.OBPreferencesControllerType;
import com.io7m.olivebench.preferences.OBPreferencesType;
import com.io7m.olivebench.preferences.OBPreferencesUndoType;
import com.io7m.olivebench.strings.OBStrings;
import com.io7m.olivebench.strings.OBStringsType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class OBControllerTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBControllerTest.class);

  private Disposable eventSub;
  private List<OBControllerEventType> eventLog;
  private OBCompositionParsersType parsers;
  private OBCompositionSerializersType serializers;
  private OBStringsType strings;
  private Observable<OBControllerEventType> events;
  private Path directory;
  private OBPreferencesControllerType preferencesController;
  private OBPreferencesUndoType preferencesUndo;
  private OBPreferencesType preferences;

  private static void checkEvents(
    final List<? extends Class<?>> eventClasses,
    final List<OBControllerEventType> eventLog)
  {
    for (int index = 0; index < eventLog.size(); ++index) {
      final var eventClass = eventLog.get(index).getClass();
      LOG.debug("[{}] event {}", Integer.valueOf(index), eventClass);
    }

    LOG.debug("checking events");

    for (int index = 0; index < eventClasses.size(); ++index) {
      final var eventClass = eventClasses.get(index);
      final var event = eventLog.get(index);

      LOG.debug(
        "[{}] {} == {}",
        Integer.valueOf(index),
        eventClass,
        event.getClass());

      Assertions.assertEquals(
        eventClass,
        event.getClass(),
        String.format(
          "[%d] %s == %s",
          Integer.valueOf(index),
          eventClass,
          event.getClass())
      );
    }

    Assertions.assertTrue(
      eventClasses.size() <= eventLog.size(),
      () -> {
        return String.format(
          "Expected event count %d <= logged event count %d",
          Integer.valueOf(eventClasses.size()),
          Integer.valueOf(eventLog.size()));
      });
  }

  @BeforeEach
  public void testSetup()
    throws IOException
  {
    this.preferencesController =
      Mockito.mock(OBPreferencesControllerType.class);
    this.preferences =
      Mockito.mock(OBPreferencesType.class);
    this.preferencesUndo =
      Mockito.mock(OBPreferencesUndoType.class);

    Mockito.when(this.preferencesController.preferences())
      .thenReturn(this.preferences);
    Mockito.when(this.preferences.undo())
      .thenReturn(this.preferencesUndo);
    Mockito.when(Integer.valueOf(this.preferencesUndo.historySize()))
      .thenReturn(Integer.valueOf(100));

    this.strings = OBStrings.of(OBStrings.getResourceBundle());
    this.parsers = OBCompositionParsers.create();
    this.serializers = OBCompositionSerializers.create();
    this.eventLog = Collections.synchronizedList(new LinkedList<>());
    this.directory = OBTestDirectories.createTempDirectory();
  }

  private OBController createController()
  {
    return OBController.create(
      this.strings,
      this.parsers,
      this.serializers,
      this.preferencesController
    );
  }

  @Test
  public void testNewComposition()
    throws Exception
  {
    try {
      try (var controller = this.createController()) {
        this.events = controller.events();
        this.eventSub = this.events.subscribe(this::logEvent);

        final var newFuture = controller.newComposition();
        newFuture.get(3L, TimeUnit.SECONDS);
        Assertions.assertFalse(controller.unsavedChanges());

        final var undoFuture0 = controller.undo();
        undoFuture0.get(3L, TimeUnit.SECONDS);
        Assertions.assertFalse(controller.unsavedChanges());

        final var closeFuture = controller.closeComposition();
        closeFuture.get(3L, TimeUnit.SECONDS);
        Assertions.assertFalse(controller.unsavedChanges());

        final var undoFuture1 = controller.undo();
        undoFuture1.get(3L, TimeUnit.SECONDS);
        Assertions.assertFalse(controller.unsavedChanges());
      }
    } finally {
      this.events.toList().blockingGet();
    }

    final var eventClasses = List.of(
      OBControllerEventTaskStarted.class,
      OBControllerEventCompositionStatusChanged.class,
      OBControllerEventCompositionStatusChanged.class,
      OBControllerEventTaskFinished.class,
      OBControllerEventTaskUndoStatusChanged.class,
      OBControllerEventTaskStarted.class,
      OBControllerEventCompositionStatusChanged.class,
      OBControllerEventTaskFinished.class,
      OBControllerEventTaskUndoStatusChanged.class
    );

    checkEvents(eventClasses, this.eventLog);
  }

  @Test
  public void testCreateChannel()
    throws Exception
  {
    try {
      try (var controller = this.createController()) {
        this.events = controller.events();
        this.eventSub = this.events.subscribe(this::logEvent);

        final var newFuture = controller.newComposition();
        newFuture.get(3L, TimeUnit.SECONDS);
        Assertions.assertFalse(controller.unsavedChanges());

        final var channelFuture = controller.createChannel(OBName.of("abc"));
        channelFuture.get(3L, TimeUnit.SECONDS);

        final var undoFuture0 = controller.undo();
        undoFuture0.get(3L, TimeUnit.SECONDS);
        Assertions.assertTrue(controller.unsavedChanges());
      }
    } finally {
      this.events.toList().blockingGet();
    }

    final var eventClasses = List.of(
      OBControllerEventTaskStarted.class,
      OBControllerEventCompositionStatusChanged.class,
      OBControllerEventCompositionStatusChanged.class,
      OBControllerEventTaskFinished.class,
      OBControllerEventTaskUndoStatusChanged.class,
      OBControllerEventTaskUndoStatusChanged.class,
      OBControllerEventTaskStarted.class,
      OBControllerEventTaskFinished.class,
      OBControllerEventCompositionChanged.class,
      OBControllerEventCompositionStatusChanged.class,
      OBControllerEventTaskUndoStatusChanged.class,
      OBControllerEventTaskStarted.class,
      OBControllerEventTaskFinished.class
    );

    checkEvents(eventClasses, this.eventLog);
  }

  @Test
  public void testCreateSaveOpen()
    throws Exception
  {
    try {
      try (var controller = this.createController()) {
        this.events = controller.events();
        this.eventSub = this.events.subscribe(this::logEvent);

        final var newFuture = controller.newComposition();
        newFuture.get(3L, TimeUnit.SECONDS);
        Assertions.assertFalse(controller.unsavedChanges());

        final var saveFuture =
          controller.saveAsComposition(this.directory.resolve("composition.xml"));
        saveFuture.get(3L, TimeUnit.SECONDS);
        Assertions.assertFalse(controller.unsavedChanges());

        final var openFuture =
          controller.openComposition(this.directory.resolve("composition.xml"));
        openFuture.get(3L, TimeUnit.SECONDS);
        Assertions.assertFalse(controller.unsavedChanges());
      }
    } finally {
      this.events.toList().blockingGet();
    }

    final var eventClasses = List.of(
      OBControllerEventTaskStarted.class,
      OBControllerEventCompositionStatusChanged.class,
      OBControllerEventCompositionStatusChanged.class,
      OBControllerEventTaskFinished.class,
      OBControllerEventTaskUndoStatusChanged.class,
      OBControllerEventTaskStarted.class,
      OBControllerEventTaskProgressChanged.class,
      OBControllerEventCompositionStatusChanged.class,
      OBControllerEventTaskFinished.class,
      OBControllerEventTaskStarted.class,
      OBControllerEventTaskProgressChanged.class,
      OBControllerEventCompositionStatusChanged.class,
      OBControllerEventCompositionStatusChanged.class,
      OBControllerEventTaskFinished.class,
      OBControllerEventTaskUndoStatusChanged.class
    );

    checkEvents(eventClasses, this.eventLog);
  }

  @Test
  public void testOpenGarbage()
    throws Exception
  {
    try {
      try (var controller = this.createController()) {
        this.events = controller.events();
        this.eventSub = this.events.subscribe(this::logEvent);

        final var path =
          Files.writeString(
            this.directory.resolve("composition.xml"),
            "Hello!");

        final var openFuture = controller.openComposition(path);
        Assertions.assertThrows(ExecutionException.class, () -> {
          openFuture.get(3L, TimeUnit.SECONDS);
        });
        Assertions.assertFalse(controller.unsavedChanges());
      }
    } finally {
      this.events.toList().blockingGet();
    }

    final var eventClasses = List.of(
      OBControllerEventTaskStarted.class,
      OBControllerEventTaskProgressChanged.class,
      OBControllerEventTaskFailed.class
    );

    checkEvents(eventClasses, this.eventLog);
  }

  private void logEvent(
    final OBControllerEventType event)
  {
    LOG.debug("event: {}", event);
    this.eventLog.add(event);
  }
}
