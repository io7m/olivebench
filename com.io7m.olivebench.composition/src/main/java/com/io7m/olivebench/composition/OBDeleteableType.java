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

/**
 * The interface supported by objects that can be "deleted".
 *
 * @param <T> The precise type of objects
 */

public interface OBDeleteableType<T extends OBDeleteableType<T>>
{
  /**
   * @return {@code true} if {@link #delete()} has been called
   */

  boolean isDeleted();

  /**
   * Delete the object. After this method has been called, subsequent
   * calls to {@code isDeleted()} will return {@code true}.
   *
   * @throws IllegalStateException If {@code isDeleted()} is {@code true}
   */

  void delete()
    throws IllegalStateException;

  /**
   * Undelete the object. After this method has been called, subsequent
   * calls to {@code isDeleted()} will return {@code false}.
   *
   * @throws IllegalStateException If {@code isDeleted()} is {@code false}
   */

  void undelete()
    throws IllegalStateException;
}
