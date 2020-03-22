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

package com.io7m.olivebench.model.metadata;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Functions over metadata values.
 */

public final class OBMetadatas
{
  private OBMetadatas()
  {

  }

  /**
   * Replace an existing property in the given metadata, or insert a new
   * one if no property exists with the given name.
   *
   * @param data  The metadata
   * @param key   The property name
   * @param value The new property value
   *
   * @return Updated metadata
   */

  public static OBMetadata put(
    final OBMetadata data,
    final String key,
    final String value)
  {
    final var without =
      data.properties()
        .stream()
        .filter(property -> !Objects.equals(property.name(), key));
    final var with =
      Stream.of(OBMetadataProperty.of(key, value));
    return OBMetadata.of(
      Stream.concat(without, with)
        .collect(Collectors.toList()));
  }
}
