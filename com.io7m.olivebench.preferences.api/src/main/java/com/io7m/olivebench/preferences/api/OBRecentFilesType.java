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

package com.io7m.olivebench.preferences.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jranges.RangeCheck;
import com.io7m.jranges.Ranges;
import org.immutables.value.Value;

import java.nio.file.Path;
import java.util.List;

/**
 * A set of recent files.
 */

@ImmutablesStyleType
@Value.Immutable
public interface OBRecentFilesType
{
  /**
   * @return The maximum size of of the set of recent files
   */

  int limit();

  /**
   * @return The recent files, organized by ascending age
   */

  List<Path> files();


  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    RangeCheck.checkIncludedInInteger(
      this.limit(),
      "Recent files limit",
      Ranges.POSITIVE_INTEGER,
      "Valid recent files limits"
    );
  }
}
