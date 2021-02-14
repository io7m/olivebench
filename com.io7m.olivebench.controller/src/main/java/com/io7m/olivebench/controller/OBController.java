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

package com.io7m.olivebench.controller;

import com.io7m.jregions.core.parameterized.areas.PAreaD;
import com.io7m.olivebench.composition.OBClockServiceType;
import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.composition.OBDublinCoreMetadata;
import com.io7m.olivebench.composition.OBLocaleServiceType;
import com.io7m.olivebench.composition.OBTimeConfiguration;
import com.io7m.olivebench.composition.OBTrackMetadata;
import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.spaces.OBWorldSpaceType;
import com.io7m.olivebench.controller.api.OBCommandContextType;
import com.io7m.olivebench.controller.api.OBCommandType;
import com.io7m.olivebench.controller.api.OBCommandUndoableExecutor;
import com.io7m.olivebench.controller.api.OBControllerCommandEvent;
import com.io7m.olivebench.controller.api.OBControllerCommandFailedEvent;
import com.io7m.olivebench.controller.api.OBControllerCompositionEvent;
import com.io7m.olivebench.controller.api.OBControllerEventType;
import com.io7m.olivebench.controller.api.OBControllerType;
import com.io7m.olivebench.controller.internal.OBCommandCompositionClose;
import com.io7m.olivebench.controller.internal.OBCommandCompositionLoad;
import com.io7m.olivebench.controller.internal.OBCommandCompositionNew;
import com.io7m.olivebench.controller.internal.OBCommandCompositionSave;
import com.io7m.olivebench.controller.internal.OBCommandCompositionTouch;
import com.io7m.olivebench.controller.internal.OBCommandStrings;
import com.io7m.olivebench.controller.internal.OBCommandTrackAdd;
import com.io7m.olivebench.controller.internal.OBCommandTrackSetMetadata;
import com.io7m.olivebench.preferences.api.OBPreferencesServiceType;
import com.io7m.olivebench.preferences.api.OBRecentFilesUpdates;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.theme.api.OBTheme;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.olivebench.controller.api.OBControllerCommandEventKind.COMMAND_ENDED;
import static com.io7m.olivebench.controller.api.OBControllerCommandEventKind.COMMAND_STARTED;
import static com.io7m.olivebench.controller.api.OBControllerCompositionEventKind.COMPOSITION_CLOSED;
import static com.io7m.olivebench.controller.api.OBControllerCompositionEventKind.COMPOSITION_OPENED;
import static com.io7m.olivebench.controller.api.OBControllerCompositionEventKind.COMPOSITION_SAVED;
import static com.io7m.olivebench.controller.api.OBControllerCompositionEventKind.COMPOSITION_UNDO_CHANGED;
import static com.io7m.olivebench.controller.api.OBControllerCompositionEventKind.COMPOSITION_VIEWPORT_CHANGED;

/**
 * The default controller implementation.
 */

public final class OBController implements OBControllerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBController.class);

  private final CompositeDisposable compositionSubscriptions;
  private final Context context;
  private final OBClockServiceType clock;
  private final OBCommandStrings strings;
  private final OBCommandUndoableExecutor executor;
  private final OBPreferencesServiceType preferences;
  private final OBServiceDirectoryType services;
  private final Subject<OBControllerEventType> events;
  private volatile OBCompositionType composition;
  private volatile OffsetDateTime timeLastSaved;
  private volatile Optional<Path> compositionFile;
  private volatile PAreaD<OBWorldSpaceType> viewport;

  private OBController(
    final OBServiceDirectoryType inServices,
    final OBCommandStrings inStrings)
  {
    this.services =
      Objects.requireNonNull(inServices, "services");
    this.clock =
      this.services.requireService(OBClockServiceType.class);
    this.preferences =
      this.services.requireService(OBPreferencesServiceType.class);

    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.events =
      PublishSubject.<OBControllerEventType>create()
        .toSerialized();

    this.executor =
      new OBCommandUndoableExecutor(256);
    this.context =
      new Context(this);

    this.timeLastSaved = this.clock.now();
    this.compositionSubscriptions = new CompositeDisposable();
    this.compositionFile = Optional.empty();
    this.viewport =
      PAreaD.of(0.0, 1.0, 0.0, 1.0);

    this.executor.events()
      .subscribe(e -> this.events.onNext(
        OBControllerCompositionEvent.of(COMPOSITION_UNDO_CHANGED)
      ));
  }

  /**
   * Create a new controller.
   *
   * @param services The service directory
   *
   * @return A new controller
   */

  public static OBControllerType create(
    final OBServiceDirectoryType services)
  {
    try {
      final var locale =
        services.requireService(OBLocaleServiceType.class);

      return new OBController(
        services,
        new OBCommandStrings(locale.locale())
      );
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void compositionSetViewport(
    final PAreaD<OBWorldSpaceType> newViewport)
  {
    this.viewport =
      Objects.requireNonNull(newViewport, "newViewport");
    this.events.onNext(
      OBControllerCompositionEvent.of(COMPOSITION_VIEWPORT_CHANGED));
  }

  @Override
  public PAreaD<OBWorldSpaceType> compositionGetViewport()
  {
    return this.viewport;
  }

  @Override
  public OBTheme theme()
  {
    return this.preferences.preferences().theme();
  }

  @Override
  public Observable<OBControllerEventType> events()
  {
    return this.events;
  }

  @Override
  public Optional<Path> compositionFile()
  {
    return this.compositionFile;
  }

  @Override
  public void compositionOpen(
    final Path file)
  {
    this.executeCommand(
      new OBCommandCompositionLoad(this.services, this.strings, file));

    this.compositionFile = Optional.of(file);
    this.addRecentFile(file);
  }

  private void addRecentFile(
    final Path file)
  {
    try {
      this.preferences.update(p -> {
        return p.withRecentFiles(
          OBRecentFilesUpdates.addRecentFile(p.recentFiles(), file)
        );
      });
    } catch (final IOException e) {
      LOG.error("unable to update recent files preferences: ", e);
    }
  }

  @Override
  public void compositionSave(
    final Path file)
  {
    this.executeCommand(
      new OBCommandCompositionSave(this.services, this.strings, file));

    this.timeLastSaved = this.composition.lastModified();
    this.compositionFile = Optional.of(file);
    this.events.onNext(OBControllerCompositionEvent.of(COMPOSITION_SAVED));
    this.addRecentFile(file);
  }

  @Override
  public void compositionNew(
    final UUID id,
    final OBTimeConfiguration timeConfiguration,
    final OBDublinCoreMetadata dcMetadata)
  {
    this.executeCommand(
      new OBCommandCompositionNew(
        this.services, this.strings, id, timeConfiguration, dcMetadata));

    this.compositionFile = Optional.empty();
  }

  @Override
  public void compositionClose()
  {
    this.executeCommand(
      new OBCommandCompositionClose(this.services, this.strings));
    this.compositionFile = Optional.empty();
  }

  @Override
  public void compositionTouch()
  {
    this.executeCommand(
      new OBCommandCompositionTouch(this.services, this.strings));
  }

  @Override
  public Optional<OBCompositionType> composition()
  {
    return Optional.ofNullable(this.composition);
  }

  @Override
  public boolean canUndo()
  {
    return this.executor.undoStackSize() > 0;
  }

  @Override
  public boolean canRedo()
  {
    return this.executor.redoStackSize() > 0;
  }

  @Override
  public void undo()
  {
    try {
      this.executor.undo(this.context);
    } catch (final Exception e) {
      this.events.onNext(OBControllerCommandFailedEvent.of(
        this.strings.format("errorUndo"),
        e.getMessage(),
        Optional.of(e)
      ));
    }
  }

  @Override
  public void redo()
  {
    try {
      this.executor.redo(this.context);
    } catch (final Exception e) {
      this.events.onNext(OBControllerCommandFailedEvent.of(
        this.strings.format("errorRedo"),
        e.getMessage(),
        Optional.of(e)
      ));
    }
  }

  @Override
  public OffsetDateTime timeLastSaved()
  {
    return this.timeLastSaved;
  }

  @Override
  public OffsetDateTime timeLastModified()
  {
    if (this.composition == null) {
      return this.timeLastSaved;
    }
    return this.composition.lastModified();
  }

  @Override
  public boolean isUnsaved()
  {
    return this.timeLastModified().isAfter(this.timeLastSaved);
  }

  @Override
  public void trackCreate()
  {
    this.executeCommand(new OBCommandTrackAdd(this.strings));
  }

  @Override
  public void trackSetMetadata(
    final OBTrackType track,
    final OBTrackMetadata newMetadata)
  {
    this.executeCommand(
      new OBCommandTrackSetMetadata(this.strings, track, newMetadata));
  }

  @Override
  public boolean trackIsActive(
    final OBTrackType track)
  {
    final var comp = this.composition;
    if (comp != null) {
      return Objects.equals(comp.tracks().firstKey(), track.id());
    }
    return false;
  }

  private void executeCommand(
    final OBCommandType command)
  {
    final var description = command.description();
    try {
      this.events.onNext(
        OBControllerCommandEvent.of(COMMAND_STARTED, description));
      this.executor.execute(this.context, command);
    } catch (final Exception e) {
      this.events.onNext(OBControllerCommandFailedEvent.of(
        description.description(),
        e.getMessage(),
        Optional.of(e)
      ));
    } finally {
      this.events.onNext(
        OBControllerCommandEvent.of(COMMAND_ENDED, description));
    }
  }

  private void onCompositionClose()
  {
    final var existing = this.composition;
    if (existing == null) {
      return;
    }

    this.events.onNext(OBControllerCompositionEvent.of(COMPOSITION_CLOSED));
    this.composition = null;
    this.compositionFile = Optional.empty();
  }

  private void onCompositionOpen(
    final OBCompositionType inComposition)
  {
    this.composition = inComposition;
    this.timeLastSaved = this.composition.lastModified();
    this.events.onNext(OBControllerCompositionEvent.of(COMPOSITION_OPENED));
  }

  @Override
  public void close()
  {

  }

  private static final class Context implements OBCommandContextType
  {
    private final OBController controller;

    private Context(
      final OBController inController)
    {
      this.controller =
        Objects.requireNonNull(inController, "controller");
    }

    @Override
    public void compositionOpen(
      final OBCompositionType composition)
    {
      this.controller.onCompositionOpen(
        Objects.requireNonNull(composition, "composition"));
    }

    @Override
    public void compositionClose()
    {
      this.controller.onCompositionClose();
    }

    @Override
    public OBCompositionType composition()
    {
      final var composition = this.controller.composition;
      if (composition == null) {
        throw new IllegalStateException("No composition is open!");
      }
      return composition;
    }

    @Override
    public void compositionSetViewport(
      final PAreaD<OBWorldSpaceType> viewport)
    {
      this.controller.compositionSetViewport(viewport);
    }
  }
}
