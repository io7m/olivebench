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

import com.io7m.olivebench.composition.OBCompositionMetadata;
import com.io7m.olivebench.composition.OBCompositions;
import com.io7m.olivebench.composition.OBTimeConfiguration;
import com.io7m.olivebench.controller.api.OBCommandContextType;
import com.io7m.olivebench.controller.api.OBCommandDescription;
import com.io7m.olivebench.controller.api.OBCommandUndoStyle;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;

import java.util.Locale;
import java.util.UUID;

public final class OBCommandCompositionNew extends OBCommand
{
  public OBCommandCompositionNew(
    final OBServiceDirectoryType inServices,
    final OBCommandStrings inStrings)
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
  }

  @Override
  public void commandDo(
    final OBCommandContextType context)
  {
    final var timeConfiguration =
      OBTimeConfiguration.builder()
        .build();

    final var meta =
      OBCompositionMetadata.builder()
        .setId(UUID.randomUUID())
        .setTimeConfiguration(timeConfiguration)
        .build();

    final var composition =
      new OBCompositions()
        .createComposition(Locale.getDefault(), meta);

    context.compositionOpen(composition);
  }
}
