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

import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.olivebench.xml.v1.OBSchemas1;

import java.util.Objects;

public final class OB1Names
{
  private OB1Names()
  {

  }

  public static BTQualifiedName dcElement(
    final String localName)
  {
    return BTQualifiedName.of(
      "http://purl.org/dc/elements/1.1/",
      Objects.requireNonNull(localName, "localName")
    );
  }

  public static BTQualifiedName element(
    final String localName)
  {
    return BTQualifiedName.of(
      OBSchemas1.namespace1().toString(),
      Objects.requireNonNull(localName, "localName")
    );
  }
}
