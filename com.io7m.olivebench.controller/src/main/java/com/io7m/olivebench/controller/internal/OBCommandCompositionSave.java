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

import com.io7m.olivebench.composition.serializer.api.OBCompositionSerializersType;
import com.io7m.olivebench.controller.api.OBCommandContextType;
import com.io7m.olivebench.controller.api.OBCommandDescription;
import com.io7m.olivebench.controller.api.OBCommandUndoStyle;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public final class OBCommandCompositionSave extends OBCommand
{
  private final Path file;

  public OBCommandCompositionSave(
    final OBServiceDirectoryType inServices,
    final OBCommandStrings inStrings,
    final Path inFile)
  {
    super(
      inServices,
      inStrings,
      OBCommandDescription.builder()
        .setDescription(inStrings.format("commandCompositionSave", inFile))
        .setLongRunning(true)
        .setUndoStyle(OBCommandUndoStyle.CANNOT_UNDO)
        .build()
    );

    this.file =
      Objects.requireNonNull(inFile, "file");
  }

  @Override
  public void commandDo(
    final OBCommandContextType context)
    throws Exception
  {
    final var services =
      this.services();
    final var serializers =
      services.requireService(OBCompositionSerializersType.class);

    final var uuid =
      UUID.randomUUID();
    final var fileTmp =
      this.file.resolveSibling(
        String.format(
          "olivebench.%s%s.tmp",
          Long.toUnsignedString(uuid.getMostSignificantBits(), 16),
          Long.toUnsignedString(uuid.getLeastSignificantBits(), 16)
        ));

    serializers.serializeAtomically(
      services,
      this.file,
      fileTmp,
      context.composition()
    );
  }
}
