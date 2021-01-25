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

package com.io7m.olivebench.composition;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

/**
 * Dublin Core metadata for a composition.
 *
 * @see "https://www.dublincore.org/specifications/dublin-core/dcmi-terms/"
 */

@ImmutablesStyleType
@Value.Immutable
public interface OBDublinCoreMetadataType
{
  /**
   * @return The title
   */

  @Value.Default
  default String title()
  {
    return "";
  }

  /**
   * @return The creator
   */

  @Value.Default
  default String creator()
  {
    return "";
  }

  /**
   * @return The subject
   */

  @Value.Default
  default String subject()
  {
    return "";
  }

  /**
   * @return The description
   */

  @Value.Default
  default String description()
  {
    return "";
  }

  /**
   * @return The publisher
   */

  @Value.Default
  default String publisher()
  {
    return "";
  }

  /**
   * @return The contributor
   */

  @Value.Default
  default String contributor()
  {
    return "";
  }

  /**
   * @return The date
   */

  @Value.Default
  default String date()
  {
    return "";
  }

  /**
   * @return The type
   */

  @Value.Default
  default String type()
  {
    return "";
  }

  /**
   * @return The format
   */

  @Value.Default
  default String format()
  {
    return "";
  }

  /**
   * @return The identifier
   */

  @Value.Default
  default String identifier()
  {
    return "";
  }

  /**
   * @return The source
   */

  @Value.Default
  default String source()
  {
    return "";
  }

  /**
   * @return The language
   */

  @Value.Default
  default String language()
  {
    return "";
  }

  /**
   * @return The relation
   */

  @Value.Default
  default String relation()
  {
    return "";
  }

  /**
   * @return The coverage
   */

  @Value.Default
  default String coverage()
  {
    return "";
  }

  /**
   * @return The rights
   */

  @Value.Default
  default String rights()
  {
    return "";
  }
}
