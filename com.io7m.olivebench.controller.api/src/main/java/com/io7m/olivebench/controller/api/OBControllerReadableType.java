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

import com.io7m.jregions.core.parameterized.areas.PAreaD;
import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.spaces.OBWorldSpaceType;
import com.io7m.olivebench.theme.api.OBTheme;
import io.reactivex.rxjava3.core.Observable;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface OBControllerReadableType
{
  /**
   * @return The composition viewport
   */

  PAreaD<OBWorldSpaceType> compositionGetViewport();

  /**
   * @return The current theme
   */

  OBTheme theme();

  /**
   * @return A stream of controller events
   */

  Observable<OBControllerEventType> events();

  /**
   * @return The file currently being used for the composition
   */

  Optional<Path> compositionFile();

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
   * @return The time that the composition was last saved to persistent storage
   */

  OffsetDateTime timeLastSaved();

  /**
   * @return The time that the composition was last modified
   */

  OffsetDateTime timeLastModified();

  /**
   * @return {@code true} if the last modification time implies that there is unsaved data
   */

  boolean isUnsaved();

  /**
   * @param track The track
   *
   * @return {@code true} if the given track is the active track
   */

  boolean trackIsActive(OBTrackType track);
}
