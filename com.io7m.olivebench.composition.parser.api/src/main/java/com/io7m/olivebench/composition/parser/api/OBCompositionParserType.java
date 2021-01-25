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

package com.io7m.olivebench.composition.parser.api;

import com.io7m.olivebench.composition.OBCompositionType;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;

/**
 * A parser.
 *
 * A parser, when executed, produces a parsed composition or a list of parse
 * errors. The correct way to use a parser instance is to call {@link #execute()}
 * and then check to see if the list returned by {@link #errors()} is non-empty.
 * If no errors were encountered, then the value returned by {@link #execute()}
 * must be non-empty.
 */

public interface OBCompositionParserType extends Closeable
{
  /**
   * @return The errors encountered during the most recent parse operation
   */

  List<OBCompositionParserError> errors();

  /**
   * Execute the parser.
   *
   * @return The parsed composition, if no errors were encountered
   */

  Optional<OBCompositionType> execute();
}
