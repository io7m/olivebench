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

package com.io7m.olivebench.composition_parser.api;

import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.services.api.OBServiceType;

import java.io.InputStream;
import java.net.URI;

/**
 * A provider of parsers.
 */

public interface OBCompositionParsersType extends OBServiceType
{
  /**
   * Create a new parser for the given input stream.
   *
   * @param services A service directory
   * @param source   The source of the stream, for diagnostic messages
   * @param stream   The input stream
   *
   * @return A new parser
   *
   * @throws UnsupportedOperationException If no formats are available
   */

  OBCompositionParserType createParser(
    OBServiceDirectoryType services,
    URI source,
    InputStream stream)
    throws UnsupportedOperationException;
}
