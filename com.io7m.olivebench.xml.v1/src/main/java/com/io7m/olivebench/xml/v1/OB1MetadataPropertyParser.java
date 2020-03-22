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
import com.io7m.blackthorne.api.BTElementParsingContextType;
import com.io7m.olivebench.model.metadata.OBMetadataProperty;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class OB1MetadataPropertyParser
  implements BTElementHandlerType<Object, OBMetadataProperty>
{
  private final OBMetadataProperty.Builder builder;

  public OB1MetadataPropertyParser()
  {
    this.builder = OBMetadataProperty.builder();
  }

  @Override
  public OBMetadataProperty onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws SAXException
  {
    try {
      this.builder.setName(attributes.getValue("name"));
      this.builder.setValue(attributes.getValue("value"));
    } catch (final Exception e) {
      throw context.parseException(e);
    }
  }
}
