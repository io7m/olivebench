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

package com.io7m.olivebench.controller.internal;

import com.io7m.jregions.core.parameterized.areas.PAreaD;
import com.io7m.olivebench.composition.OBClockServiceType;
import com.io7m.olivebench.composition.OBCompositionMetadata;
import com.io7m.olivebench.composition.OBCompositions;
import com.io7m.olivebench.composition.OBDublinCoreMetadata;
import com.io7m.olivebench.composition.OBLocaleServiceType;
import com.io7m.olivebench.composition.OBTimeConfiguration;
import com.io7m.olivebench.composition.spaces.OBWorldSpaceType;
import com.io7m.olivebench.controller.api.OBCommandContextType;
import com.io7m.olivebench.controller.api.OBCommandDescription;
import com.io7m.olivebench.controller.api.OBCommandUndoStyle;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;

import java.util.Objects;
import java.util.UUID;

public final class OBCommandCompositionNew extends OBCommand
{
  private final UUID id;
  private final OBTimeConfiguration timeConfiguration;
  private final OBDublinCoreMetadata dcMetadata;

  public OBCommandCompositionNew(
    final OBServiceDirectoryType inServices,
    final OBCommandStrings inStrings,
    final UUID inId,
    final OBTimeConfiguration inTimeConfiguration,
    final OBDublinCoreMetadata inDCMetadata)
  {
    super(
      inServices,
      inStrings,
      OBCommandDescription.builder()
        .setDescription(inStrings.format("commandCompositionNew"))
        .setLongRunning(false)
        .setUndoStyle(OBCommandUndoStyle.CLEARS_UNDO_STACK)
        .build()
    );

    this.id =
      Objects.requireNonNull(inId, "inId");
    this.timeConfiguration =
      Objects.requireNonNull(inTimeConfiguration, "timeConfiguration");
    this.dcMetadata =
      Objects.requireNonNull(inDCMetadata, "dcMetadata");
  }

  @Override
  public void commandDo(
    final OBCommandContextType context)
  {
    final var meta =
      OBCompositionMetadata.builder()
        .setId(this.id)
        .setTimeConfiguration(this.timeConfiguration)
        .setDcMetadata(this.dcMetadata)
        .build();

    final var services = this.services();
    final var composition =
      new OBCompositions()
        .createComposition(
          services.requireService(OBClockServiceType.class),
          services.requireService(OBLocaleServiceType.class),
          meta
        );

    final var viewport =
      PAreaD.<OBWorldSpaceType>of(
        (-4L * this.timeConfiguration.ticksPerQuarterNote()),
        (32L * this.timeConfiguration.ticksPerQuarterNote()),
        0L,
        128L
      );

    context.compositionOpen(composition);
    context.compositionSetViewport(viewport);
  }
}
