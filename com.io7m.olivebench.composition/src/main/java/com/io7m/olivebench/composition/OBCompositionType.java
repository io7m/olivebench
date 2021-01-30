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

package com.io7m.olivebench.composition;

import io.reactivex.rxjava3.core.Observable;

import java.time.OffsetDateTime;
import java.util.SortedMap;
import java.util.UUID;

/**
 * The type of compositions.
 */

public interface OBCompositionType
{
  /**
   * @return The composition events
   */

  Observable<OBCompositionEventType> events();

  /**
   * @return The composition metadata
   */

  OBCompositionMetadata metadata();

  /**
   * Set the metadata for the composition.
   *
   * @param metadata The metadata
   */

  void setMetadata(OBCompositionMetadata metadata);

  /**
   * @return A read-only view of the tracks in the composition
   */

  SortedMap<UUID, OBTrackType> tracks();

  /**
   * Create a new track, with an ID not equal to any existing element in the
   * composition.
   *
   * @return A new track
   */

  OBTrackType createTrack();

  /**
   * Create a new track, with the given ID (must not be equal to any existing
   * element in the composition).
   *
   * @param id The track ID
   *
   * @return A new track
   */

  OBTrackType createTrack(UUID id);

  /**
   * Return the time that the loaded composition was last modified. Note
   * that this <i>DOES NOT</i> refer to the last modified time of a composition
   * serialized to persistent storage. This value is set to the current time
   * when the composition is loaded, and is set to the current time each time
   * an operation modifies the composition.
   *
   * @return The time the loaded composition was last modified
   */

  OffsetDateTime lastModified();

  /**
   * Set the last modified time.
   *
   * @param time The time
   *
   * @see #lastModified()
   */

  void setLastModified(OffsetDateTime time);
}
