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

import com.io7m.olivebench.composition_parser.api.OBCompositionParsersType;
import com.io7m.olivebench.composition_serializer.api.OBCompositionSerializersType;
import com.io7m.olivebench.model.LoadedSaved;
import com.io7m.olivebench.model.LoadedUnsaved;
import com.io7m.olivebench.model.NotLoaded;
import com.io7m.olivebench.model.OBCompositionEventType;
import com.io7m.olivebench.model.OBCompositionReadableType;
import com.io7m.olivebench.model.OBCompositionStatusType;
import com.io7m.olivebench.model.OBCompositionType;
import com.io7m.olivebench.model.metadata.OBMetadata;
import com.io7m.olivebench.model.names.OBName;
import com.io7m.olivebench.preferences.OBPreferencesControllerType;
import com.io7m.olivebench.strings.OBStringsType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static com.io7m.olivebench.controller.OBControllerEventCompositionStatusChangedType.Status;
import static com.io7m.olivebench.model.OBCompositionStatusType.Kind.LOADED_UNSAVED;

public final class OBController implements OBControllerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBController.class);

  private final CompositeDisposable subscriptions;
  private final ExecutorService executor;
  private final LinkedList<OBControllerTaskType> redoStack;
  private final LinkedList<OBControllerTaskType> undoStack;
  private final OBCompositionParsersType parsers;
  private final OBCompositionSerializersType serializers;
  private final OBStringsType strings;
  private final Subject<OBControllerEventType> events;
  private final OBPreferencesControllerType preferences;
  private volatile OBCompositionStatusType composition;
  private volatile OBCompositionReadableType compositionLatestSnapshot;
  private volatile Disposable compositionSub;

  private OBController(
    final OBStringsType inStrings,
    final OBCompositionParsersType inParsers,
    final OBCompositionSerializersType inSerializers,
    final OBPreferencesControllerType inPreferences,
    final ExecutorService inExecutor)
  {
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.parsers =
      Objects.requireNonNull(inParsers, "inParsers");
    this.serializers =
      Objects.requireNonNull(inSerializers, "inSerializers");
    this.executor =
      Objects.requireNonNull(inExecutor, "executor");
    this.preferences =
      Objects.requireNonNull(inPreferences, "inPreferences");

    this.subscriptions =
      new CompositeDisposable();

    this.composition = NotLoaded.of(0);
    this.undoStack = new LinkedList<>();
    this.redoStack = new LinkedList<>();

    this.events =
      PublishSubject.<OBControllerEventType>create()
        .toSerialized();
  }

  public static OBController create(
    final OBStringsType inStrings,
    final OBCompositionParsersType inParsers,
    final OBCompositionSerializersType inSerializers,
    final OBPreferencesControllerType inPreferences)
  {
    final var executor =
      Executors.newSingleThreadExecutor(runnable -> {
        final var thread = new OBControllerThread(runnable);
        thread.setName(
          String.format(
            "com.io7m.olivebench.OBController[%d]",
            Long.valueOf(thread.getId())
          )
        );
        return thread;
      });

    return new OBController(
      inStrings,
      inParsers,
      inSerializers,
      inPreferences,
      executor
    );
  }

  private static Status enumStatusOf(
    final OBCompositionStatusType.Kind kind)
  {
    switch (kind) {
      case NOT_LOADED:
        return Status.STATUS_NOT_LOADED;
      case LOADED_UNSAVED:
        return Status.STATUS_UNSAVED;
      case LOADED_SAVED:
        return Status.STATUS_SAVED;
    }

    throw new IllegalStateException("Unreachable code");
  }

  @Override
  public CompletableFuture<?> newComposition()
  {
    return this.executeTask(new OBTaskNewComposition(this));
  }

  @Override
  public CompletableFuture<?> closeComposition()
  {
    return this.executeTask(new OBTaskCloseComposition(this));
  }

  @Override
  public CompletableFuture<?> createChannel(
    final OBName name)
  {
    return this.executeTask(new OBTaskCreateChannel(this, name));
  }

  @Override
  public CompletableFuture<?> openComposition(
    final Path file)
  {
    return this.executeTask(
      new OBTaskOpenComposition(
        this,
        this.parsers,
        this.preferences,
        file)
    );
  }

  @Override
  public CompletableFuture<?> saveComposition()
  {
    return this.executeTask(
      new OBTaskSaveComposition(
        this,
        this.serializers,
        this.preferences)
    );
  }

  @Override
  public CompletableFuture<?> saveAsComposition(
    final Path file)
  {
    return this.executeTask(
      new OBTaskSaveAsComposition(
        this,
        this.serializers,
        this.preferences,
        file)
    );
  }

  @Override
  public CompletableFuture<?> updateMetadata(
    final Function<OBMetadata, OBMetadata> updater)
  {
    return this.executeTask(
      new OBTaskUpdateMetadata(this, updater));
  }

  @Override
  public Optional<Path> currentFilename()
  {
    switch (this.composition.status()) {
      case NOT_LOADED:
        return Optional.empty();
      case LOADED_UNSAVED:
        return ((LoadedUnsaved) this.composition).composition().fileName();
      case LOADED_SAVED:
        return ((LoadedSaved) this.composition).composition().fileName();
    }

    throw new IllegalStateException("Unreachable code");
  }

  @Override
  public boolean unsavedChanges()
  {
    return this.composition.status() == LOADED_UNSAVED;
  }

  @Override
  public CompletableFuture<?> undo()
  {
    final var future = new CompletableFuture<>();

    this.executor.execute(() -> {
      if (this.undoStack.isEmpty()) {
        future.complete(null);
        return;
      }

      final var task = this.undoStackPop();

      try {
        this.events.onNext(
          OBControllerEventTaskStarted.builder()
            .setMessage(this.strings.controllerTaskStarted())
            .setName(task.name())
            .setLongRunning(task.isLongRunning())
            .build()
        );

        task.taskUndo();

        this.publishEvent(
          OBControllerEventTaskFinished.builder()
            .setMessage(this.strings.controllerTaskFinished())
            .setName(task.name())
            .build()
        );

        future.complete(null);

      } catch (final Exception e) {
        LOG.error("task exception: ", e);
        if (!future.isCompletedExceptionally()) {
          future.completeExceptionally(e);

          this.publishEvent(
            OBControllerEventTaskFailed.builder()
              .setException(e)
              .setMessage(e.getMessage())
              .setTitle(task.name())
              .build()
          );
        }
      }
    });

    return future;
  }

  @Override
  public OBStringsType strings()
  {
    return this.strings;
  }

  @Override
  public Observable<OBControllerEventType> events()
  {
    return this.events;
  }

  @Override
  public Optional<OBCompositionReadableType> compositionSnapshot()
  {
    return Optional.ofNullable(this.compositionLatestSnapshot);
  }

  @Override
  public void close()
  {
    this.events.onComplete();
    this.unsubscribeComposition();
    this.executor.shutdown();
  }

  void unsetComposition()
  {
    OBControllerThread.checkIsControllerThread();

    final var statusThen = enumStatusOf(this.composition.status());
    this.composition = NotLoaded.of(0);
    this.unsubscribeComposition();
    final var statusNow = enumStatusOf(this.composition.status());
    this.events.onNext(
      OBControllerEventCompositionStatusChanged.builder()
        .setStatusThen(statusThen)
        .setStatusNow(statusNow)
        .setMessage(this.strings.controllerCompositionStatusChanged())
        .build()
    );
  }

  void setComposition(
    final OBCompositionType inComposition)
  {
    OBControllerThread.checkIsControllerThread();

    LOG.debug("received new collection");
    Objects.requireNonNull(inComposition, "inComposition");

    this.unsetComposition();

    {
      final var statusThen = enumStatusOf(this.composition.status());
      this.composition = LoadedSaved.of(inComposition);
      this.publishNewCompositionSnapshot();

      this.compositionSub =
        inComposition.events()
          .subscribe(
            event -> this.executor.execute(
              () -> this.onCompositionEvent(event)));

      final var statusNow = enumStatusOf(this.composition.status());
      this.events.onNext(
        OBControllerEventCompositionStatusChanged.builder()
          .setStatusThen(statusThen)
          .setStatusNow(statusNow)
          .setMessage(this.strings.controllerCompositionStatusChanged())
          .build()
      );
    }
  }

  private void onCompositionEvent(
    final OBCompositionEventType event)
  {
    OBControllerThread.checkIsControllerThread();

    this.publishNewCompositionSnapshot();

    this.events.onNext(
      OBControllerEventCompositionChanged.builder()
        .setMessage(event.message())
        .setEvent(event)
        .build()
    );

    switch (this.composition.status()) {
      case LOADED_UNSAVED:
      case NOT_LOADED: {
        break;
      }
      case LOADED_SAVED: {
        this.setUnsaved();
        break;
      }
    }
  }

  private void publishNewCompositionSnapshot()
  {
    switch (this.composition.status()) {
      case NOT_LOADED: {
        this.compositionLatestSnapshot = null;
        LOG.debug("dropped snapshot");
        break;
      }
      case LOADED_UNSAVED: {
        this.compositionLatestSnapshot =
          ((LoadedUnsaved) this.composition).composition()
            .snapshot();
        LOG.debug("published new snapshot");
        break;
      }
      case LOADED_SAVED: {
        this.compositionLatestSnapshot =
          ((LoadedSaved) this.composition).composition()
            .snapshot();
        LOG.debug("published new snapshot");
        break;
      }
    }
  }

  private void setUnsaved()
  {
    OBControllerThread.checkIsControllerThread();

    final var then = enumStatusOf(this.composition.status());
    this.composition = LoadedUnsaved.of(this.composition());
    final var now = enumStatusOf(this.composition.status());
    this.events.onNext(
      OBControllerEventCompositionStatusChanged.builder()
        .setStatusThen(then)
        .setStatusNow(now)
        .setMessage(this.strings.controllerCompositionStatusChanged())
        .build()
    );
  }

  void setSaved()
  {
    OBControllerThread.checkIsControllerThread();

    final var then = enumStatusOf(this.composition.status());
    this.composition = LoadedSaved.of(this.composition());
    final var now = enumStatusOf(this.composition.status());
    this.events.onNext(
      OBControllerEventCompositionStatusChanged.builder()
        .setStatusThen(then)
        .setStatusNow(now)
        .setMessage(this.strings.controllerCompositionStatusChanged())
        .build()
    );
  }

  private void unsubscribeComposition()
  {
    final var sub = this.compositionSub;
    if (sub != null) {
      sub.dispose();
    }
  }

  public void publishEvent(
    final OBControllerEventType event)
  {
    this.events.onNext(event);
  }

  private void undoStackClear()
  {
    this.undoStack.clear();
    this.undoStackPublish();
  }

  private void undoStackPublish()
  {
    final var topTask = this.undoStack.peek();
    if (topTask != null) {
      this.publishEvent(
        OBControllerEventTaskUndoStatusChanged.builder()
          .setMessage(this.strings.controllerUndoStatusChanged())
          .setAvailable(Optional.of(topTask.name()))
          .build()
      );
    } else {
      this.publishEvent(
        OBControllerEventTaskUndoStatusChanged.builder()
          .setMessage(this.strings.controllerUndoStatusChanged())
          .setAvailable(Optional.empty())
          .build()
      );
    }
  }

  private void undoStackAdd(
    final OBControllerTaskType task)
  {
    final var undoSettings = this.preferences.preferences().undo();
    if (this.undoStack.size() >= undoSettings.historySize()) {
      this.undoStack.removeFirst();
    }
    this.undoStack.push(task);
    this.undoStackPublish();
  }

  private OBControllerTaskType undoStackPop()
  {
    final var result = this.undoStack.pop();
    this.undoStackPublish();
    return result;
  }

  private CompletableFuture<?> executeTask(
    final OBControllerTaskType task)
  {
    final var future = new CompletableFuture<>();

    this.executor.execute(() -> {
      switch (task.undoStyle()) {
        case CAN_UNDO: {
          this.undoStackAdd(task);
          break;
        }
        case CANNOT_UNDO:
        case CLEARS_UNDO_STACK: {
          break;
        }
      }

      try {
        this.publishEvent(
          OBControllerEventTaskStarted.builder()
            .setMessage(this.strings.controllerTaskStarted())
            .setName(task.name())
            .setLongRunning(task.isLongRunning())
            .build()
        );

        task.taskDo();

        this.publishEvent(
          OBControllerEventTaskFinished.builder()
            .setMessage(this.strings.controllerTaskFinished())
            .setName(task.name())
            .build()
        );

        future.complete(null);

        switch (task.undoStyle()) {
          case CLEARS_UNDO_STACK: {
            this.undoStackClear();
            break;
          }
          case CAN_UNDO:
          case CANNOT_UNDO: {
            break;
          }
        }
      } catch (final Exception e) {
        LOG.error("task exception: ", e);
        if (!future.isCompletedExceptionally()) {
          future.completeExceptionally(e);

          this.publishEvent(
            OBControllerEventTaskFailed.builder()
              .setException(e)
              .setMessage(e.getMessage())
              .setTitle(task.name())
              .build()
          );
        }
      }
    });

    return future;
  }

  OBCompositionType composition()
  {
    switch (this.composition.status()) {
      case NOT_LOADED:
        throw new IllegalStateException("No composition is loaded");
      case LOADED_UNSAVED:
        return ((LoadedUnsaved) this.composition).composition();
      case LOADED_SAVED:
        return ((LoadedSaved) this.composition).composition();
    }

    throw new IllegalStateException("Unreachable code");
  }

  OBPreferencesControllerType preferences()
  {
    return this.preferences;
  }

  void savePreferences()
  {
    OBControllerThread.checkIsControllerThread();
  }
}
