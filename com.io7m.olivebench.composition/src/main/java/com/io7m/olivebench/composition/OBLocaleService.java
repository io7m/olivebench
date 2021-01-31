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

import java.util.Locale;
import java.util.Objects;

/**
 * The default locale service implementation.
 */

public final class OBLocaleService implements OBLocaleServiceType
{
  private final Locale locale;

  /**
   * Construct a service.
   *
   * @param inLocale The locale
   */

  public OBLocaleService(
    final Locale inLocale)
  {
    this.locale = Objects.requireNonNull(inLocale, "locale");
  }

  /**
   * Construct a service.
   */

  public OBLocaleService()
  {
    this(Locale.getDefault());
  }

  @Override
  public Locale locale()
  {
    return this.locale;
  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBLocaleService 0x%08x]",
      Integer.valueOf(this.hashCode())
    );
  }
}
