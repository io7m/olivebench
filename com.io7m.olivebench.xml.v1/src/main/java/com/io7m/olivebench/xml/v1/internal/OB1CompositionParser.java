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
import com.io7m.olivebench.composition.OBCompositionFactoryType;
import com.io7m.olivebench.composition.OBCompositionMetadata;
import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;

import java.time.Clock;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.io7m.olivebench.xml.v1.internal.OB1Names.element;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public final class OB1CompositionParser
  implements BTElementHandlerType<Object, OBCompositionType>
{
  private final Clock clock;
  private final Locale locale;
  private final OBCompositionFactoryType factory;
  private OBCompositionMetadata metadata;
  private OBCompositionType composition;

  public OB1CompositionParser(
    final Clock inClock,
    final Locale inLocale,
    final OBServiceDirectoryType services)
  {
    this.clock =
      Objects.requireNonNull(inClock, "clock");
    this.locale =
      Objects.requireNonNull(inLocale, "locale");
    this.factory =
      services.requireService(OBCompositionFactoryType.class);
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return ofEntries(
      entry(
        element("Metadata"),
        c -> new OB1MetadataParser()),
      entry(
        element("Tracks"),
        c -> new OB1TracksParser(this.composition))
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof OBCompositionMetadata) {
      this.metadata = (OBCompositionMetadata) result;
      this.composition =
        this.factory.createComposition(this.clock, this.locale, this.metadata);
      return;
    }
    if (result instanceof OB1Tracks) {
      return;
    }

    throw new IllegalStateException(
      String.format("Unrecognized value: %s", result)
    );
  }

  @Override
  public OBCompositionType onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.composition;
  }
}
