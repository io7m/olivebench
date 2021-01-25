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

package com.io7m.olivebench.composition.serializer.spi;

import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.services.api.OBServiceType;

import java.io.OutputStream;
import java.net.URI;
import java.util.Comparator;
import java.util.Objects;

/**
 * A serializer provider.
 */

public interface OBCompositionSPISerializersType
  extends OBServiceType, Comparable<OBCompositionSPISerializersType>
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
    OBCompositionType composition);

  @Override
  default int compareTo(
    final OBCompositionSPISerializersType other)
  {
    Objects.requireNonNull(other, "other");
    return Comparator.comparingInt(OBCompositionSPISerializersType::versionMajor)
      .thenComparingInt(OBCompositionSPISerializersType::versionMinor)
      .compare(this, other);
  }
}
