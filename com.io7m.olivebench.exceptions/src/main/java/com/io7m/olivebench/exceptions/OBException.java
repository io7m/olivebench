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

package com.io7m.olivebench.exceptions;

import com.io7m.olivebench.presentable.OBPresentableErrorType;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public abstract class OBException
  extends Exception implements OBPresentableErrorType
{
  private final Map<String, String> attributes;

  public OBException(
    final String message,
    final Map<String, String> inAttributes)
  {
    super(Objects.requireNonNull(message, "message"));
    this.attributes =
      Map.copyOf(Objects.requireNonNull(inAttributes, "attributes"));
  }

  public OBException(
    final String message)
  {
    this(
      Objects.requireNonNull(message, "message"),
      Collections.emptyMap());
  }

  public OBException(
    final String message,
    final Throwable cause)
  {
    this(
      Objects.requireNonNull(message, "message"),
      Objects.requireNonNull(cause, "cause"),
      Collections.emptyMap());
  }

  public OBException(
    final Throwable cause)
  {
    this(
      Objects.requireNonNull(cause, "cause"),
      Collections.emptyMap());
  }

  public OBException(
    final String message,
    final Throwable cause,
    final Map<String, String> inAttributes)
  {
    super(
      Objects.requireNonNull(message, "message"),
      Objects.requireNonNull(cause, "cause"));
    this.attributes =
      Map.copyOf(Objects.requireNonNull(inAttributes, "attributes"));
  }

  public OBException(
    final Throwable cause,
    final Map<String, String> inAttributes)
  {
    super(Objects.requireNonNull(cause, "cause"));
    this.attributes =
      Map.copyOf(Objects.requireNonNull(inAttributes, "attributes"));
  }

  @Override
  public final Map<String, String> attributes()
  {
    return this.attributes;
  }

  @Override
  public final String message()
  {
    return this.getLocalizedMessage();
  }
}
