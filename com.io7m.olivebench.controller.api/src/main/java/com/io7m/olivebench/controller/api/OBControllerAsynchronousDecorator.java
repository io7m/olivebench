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

package com.io7m.olivebench.controller.api;

import com.io7m.olivebench.composition.OBCompositionType;
import io.reactivex.rxjava3.core.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An asynchronous decorator for a controller.
 */

public final class OBControllerAsynchronousDecorator
  implements OBControllerAsynchronousType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBControllerAsynchronousDecorator.class);

  private final ExecutorService executor;
  private final OBControllerType delegate;

  private OBControllerAsynchronousDecorator(
    final ExecutorService inExecutor,
    final OBControllerType inDelegate)
  {
    this.executor =
      Objects.requireNonNull(inExecutor, "inExecutor");
    this.delegate =
      Objects.requireNonNull(inDelegate, "delegate");
  }

  /**
   * Create an asynchronous controller from the existing controller.
   *
   * @param delegate The delegate controller
   *
   * @return A new controller
   */

  public static OBControllerAsynchronousType create(
    final OBControllerType delegate)
  {
    return new OBControllerAsynchronousDecorator(
      Executors.newSingleThreadExecutor(runnable -> {
        final var thread = new Thread(runnable);
        thread.setName(String.format(
          "com.io7m.olivebench.controller[%d]",
          Long.valueOf(thread.getId()))
        );
        return thread;
      }),
      delegate
    );
  }

  @Override
  public Observable<OBControllerEventType> events()
  {
    return this.delegate.events();
  }

  @Override
  public Optional<Path> compositionFile()
  {
    return this.delegate.compositionFile();
  }

  @Override
  public void compositionOpen(
    final Path file)
  {
    this.executor.execute(() -> this.delegate.compositionOpen(file));
  }

  @Override
  public void compositionSave(
    final Path file)
  {
    this.executor.execute(() -> this.delegate.compositionSave(file));
  }

  @Override
  public void compositionClose()
  {
    this.executor.execute(this.delegate::compositionClose);
  }

  @Override
  public void compositionTouch()
  {
    this.executor.execute(this.delegate::compositionTouch);
  }

  @Override
  public Optional<OBCompositionType> composition()
  {
    return this.delegate.composition();
  }

  @Override
  public boolean canUndo()
  {
    return this.delegate.canUndo();
  }

  @Override
  public boolean canRedo()
  {
    return this.delegate.canRedo();
  }

  @Override
  public void undo()
  {
    this.executor.execute(this.delegate::undo);
  }

  @Override
  public void redo()
  {
    this.executor.execute(this.delegate::redo);
  }

  @Override
  public OffsetDateTime timeLastSaved()
  {
    return this.delegate.timeLastSaved();
  }

  @Override
  public OffsetDateTime timeLastModified()
  {
    return this.delegate.timeLastModified();
  }

  @Override
  public boolean isUnsaved()
  {
    return this.delegate.isUnsaved();
  }

  @Override
  public void close()
  {
    this.executor.execute(() -> {
      try {
        this.delegate.close();
      } catch (final IOException e) {
        LOG.error("i/o error: ", e);
      }
    });
    this.executor.shutdown();
  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBController 0x%08x]",
      Integer.valueOf(this.hashCode())
    );
  }
}
