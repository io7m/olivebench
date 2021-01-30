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
import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.jxe.core.JXESchemaDefinition;
import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.composition.parser.spi.OBCompositionSPIParsersType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.xml.v1.internal.OB1CompositionParser;

import java.time.Clock;
import java.util.List;
import java.util.Locale;

import static com.io7m.olivebench.xml.v1.internal.OB1Names.element;

public final class OBCompositionParserV1 implements OBCompositionSPIParsersType
{
  public OBCompositionParserV1()
  {

  }

  @Override
  public List<JXESchemaDefinition> schemas()
  {
    return OBSchemas1.schemas1();
  }

  @Override
  public BTQualifiedName rootName()
  {
    return element("Composition");
  }

  @Override
  public BTElementHandlerType<?, OBCompositionType> createHandler(
    final Clock clock,
    final Locale locale,
    final OBServiceDirectoryType services)
  {
    return new OB1CompositionParser(clock, locale, services);
  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBCompositionParserV1 0x%08x]",
      Integer.valueOf(this.hashCode())
    );
  }
}
