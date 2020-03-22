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

package com.io7m.olivebench.composition_serializer.spi;

import com.io7m.olivebench.model.OBCompositionReadableType;

import java.io.OutputStream;
import java.net.URI;

/**
 * A serializer provider.
 */

public interface OBCompositionSPISerializersType
{
  /**
   * @return The supported major format version
   */

  int versionMajor();

  /**
   * @return The supported minor format version
   */

  int versionMinor();

  /**
   * Create a new serializer for the given composition.
   *
   * @param target      The target URI
   * @param output      The output stream
   * @param composition The composition
   *
   * @return A new serializer
   */

  OBCompositionSPISerializerType create(
    URI target,
    OutputStream output,
    OBCompositionReadableType composition);
}
