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

package com.io7m.olivebench.composition_parser.spi;

import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.jxe.core.JXESchemaDefinition;
import com.io7m.olivebench.model.OBCompositionType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;

/**
 * The SPI implemented by parsers.
 *
 * A parser implementation is required to produce a <i>schema</i> indicating
 * which XML format it supports. It is also required to produce a <i>handler</i>
 * on demand that is capable of parsing formats that conform to the schema
 * the parser returned.
 */

public interface OBCompositionSPIParsersType
{
  /**
   * The schema that describes documents that this parser implementation
   * can handle.
   *
   * @return The schema
   */

  JXESchemaDefinition schema();

  /**
   * @param services A service directory
   *
   * @return A content handler for parsing documents
   */

  BTElementHandlerType<?, OBCompositionType> createHandler(
    OBServiceDirectoryType services);
}
