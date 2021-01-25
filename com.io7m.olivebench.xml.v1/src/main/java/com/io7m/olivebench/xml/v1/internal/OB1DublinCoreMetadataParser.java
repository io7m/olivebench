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

package com.io7m.olivebench.xml.v1.internal;

import com.io7m.blackthorne.api.BTElementHandlerConstructorType;
import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.blackthorne.api.BTElementParsingContextType;
import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.olivebench.composition.OBDublinCoreMetadata;

import java.util.Map;

import static com.io7m.olivebench.xml.v1.internal.DCElementHandlers.constructor;
import static com.io7m.olivebench.xml.v1.internal.OB1Names.dcElement;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public final class OB1DublinCoreMetadataParser
  implements BTElementHandlerType<Object, OBDublinCoreMetadata>
{
  private final OBDublinCoreMetadata.Builder metadata;

  public OB1DublinCoreMetadataParser()
  {
    this.metadata = OBDublinCoreMetadata.builder();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return ofEntries(
      entry(dcElement("contributor"), constructor("contributor")),
      entry(dcElement("coverage"), constructor("coverage")),
      entry(dcElement("creator"), constructor("creator")),
      entry(dcElement("date"), constructor("date")),
      entry(dcElement("description"), constructor("description")),
      entry(dcElement("format"), constructor("format")),
      entry(dcElement("identifier"), constructor("identifier")),
      entry(dcElement("language"), constructor("language")),
      entry(dcElement("publisher"), constructor("publisher")),
      entry(dcElement("relation"), constructor("relation")),
      entry(dcElement("rights"), constructor("rights")),
      entry(dcElement("source"), constructor("source")),
      entry(dcElement("subject"), constructor("subject")),
      entry(dcElement("title"), constructor("title")),
      entry(dcElement("type"), constructor("type"))
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof DCElement) {
      final var dcElement = (DCElement) result;
      this.onDCElement(dcElement);
      return;
    }

    throw new IllegalStateException(
      String.format("Unrecognized value: %s", result)
    );
  }

  // CHECKSTYLE:OFF
  private void onDCElement(
    final DCElement dcElement)
  // CHECKSTYLE:ON
  {
    final var value = dcElement.value();
    switch (dcElement.name()) {
      case "title": {
        this.metadata.setTitle(value);
        return;
      }
      case "creator": {
        this.metadata.setCreator(value);
        return;
      }
      case "subject": {
        this.metadata.setSubject(value);
        return;
      }
      case "description": {
        this.metadata.setDescription(value);
        return;
      }
      case "publisher": {
        this.metadata.setPublisher(value);
        return;
      }
      case "contributor": {
        this.metadata.setContributor(value);
        return;
      }
      case "date": {
        this.metadata.setDate(value);
        return;
      }
      case "type": {
        this.metadata.setType(value);
        return;
      }
      case "format": {
        this.metadata.setFormat(value);
        return;
      }
      case "identifier": {
        this.metadata.setIdentifier(value);
        return;
      }
      case "source": {
        this.metadata.setSource(value);
        return;
      }
      case "language": {
        this.metadata.setLanguage(value);
        return;
      }
      case "relation": {
        this.metadata.setRelation(value);
        return;
      }
      case "coverage": {
        this.metadata.setCoverage(value);
        return;
      }
      case "rights": {
        this.metadata.setRights(value);
        return;
      }
      default: {
        throw new IllegalStateException(
          String.format("Unexpected value: %s", dcElement.name())
        );
      }
    }
  }

  @Override
  public OBDublinCoreMetadata onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.metadata.build();
  }
}
