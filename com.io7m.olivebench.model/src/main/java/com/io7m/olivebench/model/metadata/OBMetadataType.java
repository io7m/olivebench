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

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A set of metadata properties.
 */

@ImmutablesStyleType
@Value.Immutable
public interface OBMetadataType
{
  /**
   * @return The metadata properties
   */

  @Value.Parameter
  List<OBMetadataProperty> properties();

  /**
   * Find a property with the given name.
   *
   * @param name A name
   *
   * @return A property if one exists
   */

  default Optional<OBMetadataProperty> find(
    final String name)
  {
    Objects.requireNonNull(name, "name");
    return this.properties()
      .stream()
      .filter(prop -> Objects.equals(prop.name(), name))
      .findFirst();
  }

  /**
   * Find a property with the given name.
   *
   * @param name A name
   *
   * @return A property value if one exists
   */

  default Optional<String> findValue(
    final String name)
  {
    return this.find(name)
      .map(OBMetadataProperty::value);
  }

  /**
   * Find a property with the given name.
   *
   * @param name      A name
   * @param otherwise The default value if no property exists
   *
   * @return A property value if one exists, or {@code otherwise}
   */

  default String findValueOrDefault(
    final String name,
    final String otherwise)
  {
    return this.findValue(name).orElse(otherwise);
  }
}
