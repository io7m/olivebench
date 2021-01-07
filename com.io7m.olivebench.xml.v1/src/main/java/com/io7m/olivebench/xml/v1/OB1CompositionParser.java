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
import com.io7m.olivebench.model.OBComposition;
import com.io7m.olivebench.model.OBCompositionType;
import com.io7m.olivebench.model.graph.OBCompositionGraphType;
import com.io7m.olivebench.model.metadata.OBCompositionMetadata;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.strings.OBStringsType;

import java.util.Map;
import java.util.Objects;

public final class OB1CompositionParser
  implements BTElementHandlerType<Object, OBCompositionType>
{
  private final OBServiceDirectoryType services;
  private OBCompositionGraphType graph;
  private OBCompositionType composition;
  private OBCompositionMetadata metadata;

  private OB1CompositionParser(
    final OBServiceDirectoryType inServices)
  {
    this.services = Objects.requireNonNull(inServices, "services");
  }

  public static BTElementHandlerType<Object, OBCompositionType> create(
    final OBServiceDirectoryType inServices)
  {
    Objects.requireNonNull(inServices, "services");
    inServices.requireService(OBStringsType.class);
    return new OB1CompositionParser(inServices);
  }

  @Override
  public OBCompositionType onElementFinished(
    final BTElementParsingContextType context)
  {
    this.composition = OBComposition.createWith(this.services, this.graph);
    this.composition.metadata().set(this.metadata);
    return this.composition;
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    final var namespace = OB1Schemas.NAMESPACE_1_URI.toString();
    return Map.ofEntries(
      Map.entry(
        BTQualifiedName.of(namespace, "Metadata"),
        ignored -> new OB1MetadataParser()
      ),
      Map.entry(
        BTQualifiedName.of(namespace, "Graph"),
        ignored -> new OB1GraphParser(this.services)
      )
    );
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof OBCompositionMetadata) {
      this.metadata = (OBCompositionMetadata) result;
    } else if (result instanceof OBCompositionGraphType) {
      this.graph = (OBCompositionGraphType) result;
    }
  }
}
