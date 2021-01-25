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

package com.io7m.olivebench.xml.v1.internal;

import com.io7m.blackthorne.api.BTElementHandlerConstructorType;
import com.io7m.blackthorne.api.Blackthorne;

import static com.io7m.olivebench.xml.v1.internal.OB1Names.dcElement;

public final class DCElementHandlers
{
  private DCElementHandlers()
  {

  }

  public static BTElementHandlerConstructorType<?, DCElement> constructor(
    final String type)
  {
    return Blackthorne.forScalar(
      dcElement(type),
      (context, characters, offset, length) ->
        DCElement.of(type, stringOf(characters, offset, length))
    );
  }

  private static String stringOf(
    final char[] characters,
    final int offset,
    final int length)
  {
    // CHECKSTYLE:OFF
    return new String(characters, offset, length);
    // CHECKSTYLE:ON
  }
}
