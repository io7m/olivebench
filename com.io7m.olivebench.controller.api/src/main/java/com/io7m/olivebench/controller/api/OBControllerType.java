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

import java.nio.file.Path;
import java.util.Optional;

/**
 * The (synchronous) controller API.
 */

public interface OBControllerType
{
  /**
   * @return A stream of controller events
   */

  Observable<OBControllerEventType> events();

  /**
   * Open a composition.
   *
   * @param file The composition file
   */

  void compositionOpen(Path file);

  /**
   * Close the open composition.
   */

  void compositionClose();

  /**
   * @return The open composition, if any
   */

  Optional<OBCompositionType> composition();

  /**
   * @return {@code true} if the undo stack is not empty
   */

  boolean canUndo();

  /**
   * @return {@code true} if the redo stack is not empty
   */

  boolean canRedo();

  /**
   * Undo the last command.
   */

  void undo();

  /**
   * Redo the last command.
   */

  void redo();
}
