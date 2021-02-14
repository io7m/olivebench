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
import com.io7m.olivebench.composition.OBDublinCoreMetadata;
import com.io7m.olivebench.composition.OBTimeConfiguration;
import com.io7m.olivebench.composition.OBTrackMetadata;
import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.spaces.OBWorldSpaceType;
import com.io7m.olivebench.services.api.OBServiceType;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.UUID;

/**
 * The (synchronous) controller API.
 */

public interface OBControllerType extends Closeable, OBServiceType,
  OBControllerReadableType
{
  /**
   * Set the composition viewport.
   *
   * @param newViewport The new viewport
   */

  void compositionSetViewport(
    PAreaD<OBWorldSpaceType> newViewport);

  /**
   * Open a composition.
   *
   * @param file The composition file
   */

  void compositionOpen(Path file);

  /**
   * Save a composition.
   *
   * @param file The composition file
   */

  void compositionSave(Path file);

  /**
   * Create a new composition.
   *
   * @param id                The composition ID
   * @param timeConfiguration The time configuration
   * @param dcMetadata        The Dublin Core metadata
   */

  void compositionNew(
    UUID id,
    OBTimeConfiguration timeConfiguration,
    OBDublinCoreMetadata dcMetadata);

  /**
   * Create a new composition.
   */

  default void compositionNew()
  {
    this.compositionNew(
      UUID.randomUUID(),
      OBTimeConfiguration.builder()
        .build(),
      OBDublinCoreMetadata.builder()
        .build()
    );
  }

  /**
   * Close the open composition.
   */

  void compositionClose();

  /**
   * Touch the open composition, marking it as having unsaved data.
   */

  void compositionTouch();

  /**
   * Undo the last command.
   */

  void undo();

  /**
   * Redo the last command.
   */

  void redo();

  /**
   * Create a new track in the composition.
   */

  void trackCreate();

  /**
   * Replace the metadata for a given track.
   *
   * @param track       The track
   * @param newMetadata The new metadata
   */

  void trackSetMetadata(
    OBTrackType track,
    OBTrackMetadata newMetadata);

}
