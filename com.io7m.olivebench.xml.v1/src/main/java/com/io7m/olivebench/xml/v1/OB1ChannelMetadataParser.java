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
import com.io7m.jtensors.core.parameterized.vectors.PVector3D;
import com.io7m.olivebench.model.graph.OBChannelMetadata;
import com.io7m.olivebench.model.spaces.OBSpaceRGBType;
import org.xml.sax.Attributes;

import java.util.Map;

public final class OB1ChannelMetadataParser
  implements BTElementHandlerType<Object, OBChannelMetadata>
{
  private final OBChannelMetadata.Builder builder;

  public OB1ChannelMetadataParser()
  {
    this.builder = OBChannelMetadata.builder();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    final var namespace = OB1Schemas.NAMESPACE_1_URI.toString();
    return Map.ofEntries(
      Map.entry(
        BTQualifiedName.of(namespace, "Color"),
        context1 -> new OB1ColorParser()
      )
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof PVector3D) {
      this.builder.setColor((PVector3D<OBSpaceRGBType>) result);
    }
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {

  }

  @Override
  public OBChannelMetadata onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }
}
