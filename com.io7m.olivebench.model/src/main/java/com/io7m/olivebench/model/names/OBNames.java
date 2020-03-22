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

package com.io7m.olivebench.model.names;

public final class OBNames
{
  private OBNames()
  {

  }

  public static boolean isValid(
    final String inName)
  {
    return inName.length() <= 128;
  }

  public static String checkValid(
    final String inName)
  {
    if (!isValid(inName)) {
      final var builder =
        new StringBuilder(128)
          .append("Illegal name.")
          .append(System.lineSeparator())
          .append("  Expected: A name of length <= 128")
          .append(System.lineSeparator())
          .append("  Received: ")
          .append(inName)
          .append(System.lineSeparator())
          .toString();
      throw new IllegalArgumentException(builder);
    }
    return inName;
  }
}
