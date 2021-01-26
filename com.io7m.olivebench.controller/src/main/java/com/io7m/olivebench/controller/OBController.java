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

import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.controller.api.OBCommandContextType;
import com.io7m.olivebench.controller.api.OBCommandType;
import com.io7m.olivebench.controller.api.OBCommandUndoableExecutor;
import com.io7m.olivebench.controller.api.OBControllerCommandFailedEvent;
import com.io7m.olivebench.controller.api.OBControllerCompositionEvent;
import com.io7m.olivebench.controller.api.OBControllerEventType;
import com.io7m.olivebench.controller.api.OBControllerType;
import com.io7m.olivebench.controller.internal.OBCommandCompositionClose;
import com.io7m.olivebench.controller.internal.OBCommandCompositionLoad;
import com.io7m.olivebench.controller.internal.OBCommandStrings;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.olivebench.controller.api.OBControllerCompositionEventKind.COMPOSITION_CLOSED;
import static com.io7m.olivebench.controller.api.OBControllerCompositionEventKind.COMPOSITION_OPENED;
import static com.io7m.olivebench.controller.api.OBControllerCompositionEventKind.COMPOSITION_UNDO_CHANGED;

/**
 * The default controller implementation.
 */

public final class OBController implements OBControllerType
{
  private final OBServiceDirectoryType services;
  private final OBCommandStrings strings;
  private final Subject<OBControllerEventType> events;
  private final OBCommandUndoableExecutor executor;
  private final Context context;
  private volatile OBCompositionType composition;

  private OBController(
    final OBServiceDirectoryType inServices,
    final OBCommandStrings inStrings)
  {
    this.services =
      Objects.requireNonNull(inServices, "services");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.events =
      PublishSubject.<OBControllerEventType>create()
        .toSerialized();
    this.executor =
      new OBCommandUndoableExecutor(256);
    this.context =
      new Context(this);

    this.executor.events()
      .subscribe(e -> this.events.onNext(
        OBControllerCompositionEvent.of(COMPOSITION_UNDO_CHANGED)
      ));
  }

  /**
   * Create a new controller.
   *
   * @param services The service directory
   * @param locale   The locale
   *
   * @return A new controller
   */

  public static OBControllerType create(
    final OBServiceDirectoryType services,
    final Locale locale)
  {
    try {
      return new OBController(services, new OBCommandStrings(locale));
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Observable<OBControllerEventType> events()
  {
    return this.events;
  }

  @Override
  public void compositionOpen(
    final Path file)
  {
    this.executeCommand(
      new OBCommandCompositionLoad(this.services, this.strings, file));
  }

  @Override
  public void compositionClose()
  {
    this.executeCommand(
      new OBCommandCompositionClose(this.services, this.strings));
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
        e.getMessage(),
        Optional.of(e)
      ));
    }
  }

  private void executeCommand(
    final OBCommandType command)
  {
    try {
      this.executor.execute(this.context, command);
    } catch (final Exception e) {
      this.events.onNext(OBControllerCommandFailedEvent.of(
        e.getMessage(),
        Optional.of(e)
      ));
    }
  }

  private void onCompositionClose()
  {
    final var existing = this.composition;
    if (existing == null) {
      throw new IllegalStateException(
        this.strings.format("compositionNotOpen"));
    }

    this.events.onNext(OBControllerCompositionEvent.of(COMPOSITION_CLOSED));
    this.composition = null;
  }

  private void onCompositionOpen(
    final OBCompositionType inComposition)
  {
    this.composition = inComposition;
    this.events.onNext(OBControllerCompositionEvent.of(COMPOSITION_OPENED));
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
  }
}
