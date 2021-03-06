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

package com.io7m.olivebench.xml.v1;

import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.jxe.core.JXESchemaDefinition;
import com.io7m.olivebench.composition_parser.spi.OBCompositionSPIParsersType;
import com.io7m.olivebench.model.OBCompositionType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;

/**
 * A provider of parsers for the 1.0 XML collection format.
 */

public final class OB1Parsers implements OBCompositionSPIParsersType
{
  /**
   * Construct a provider.
   */

  public OB1Parsers()
  {

  }

  @Override
  public JXESchemaDefinition schema()
  {
    return OB1Schemas.SCHEMA_1;
  }

  @Override
  public BTElementHandlerType<?, OBCompositionType> createHandler(
    final OBServiceDirectoryType services)
  {
    return OB1CompositionParser.create(services);
  }

  @Override
  public String toString()
  {
    return String.format(
      "[%s %s]",
      this.getClass().getCanonicalName(),
      this.schema().namespace()
    );
  }
}
