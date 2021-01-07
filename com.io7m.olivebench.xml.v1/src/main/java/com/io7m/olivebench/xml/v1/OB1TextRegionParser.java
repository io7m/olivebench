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

import com.io7m.blackthorne.api.BTElementHandlerConstructorType;
import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.blackthorne.api.BTElementParsingContextType;
import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.olivebench.model.graph.OBNodeMetadata;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Map;
import java.util.UUID;

public final class OB1TextRegionParser
  implements BTElementHandlerType<OBNodeMetadata, OB1TextRegion>
{
  private final OB1TextRegion.Builder builder;

  public OB1TextRegionParser()
  {
    this.builder = OB1TextRegion.builder();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends OBNodeMetadata>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    final var namespace = OB1Schemas.NAMESPACE_1_URI.toString();
    return Map.ofEntries(
      Map.entry(
        BTQualifiedName.of(namespace, "NodeMetadata"),
        context1 -> new OB1NodeMetadataParser()
      )
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final OBNodeMetadata result)
  {
    this.builder.setNodeMetadata(result);
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws SAXException
  {
    try {
      this.builder.setId(
        UUID.fromString(attributes.getValue("id")));
      this.builder.setText(
        attributes.getValue("text"));
    } catch (final Exception e) {
      throw context.parseException(e);
    }
  }

  @Override
  public OB1TextRegion onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }
}
