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

import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.blackthorne.api.BTElementParsingContextType;
import com.io7m.olivebench.composition.OBTimeConfiguration;
import org.xml.sax.Attributes;

import static java.lang.Long.parseUnsignedLong;

public final class OB1TimeConfigurationParser
  implements BTElementHandlerType<Object, OBTimeConfiguration>
{
  private final OBTimeConfiguration.Builder timeConfiguration;

  public OB1TimeConfigurationParser()
  {
    this.timeConfiguration = OBTimeConfiguration.builder();
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    final var ticksPerQuarterNote =
      parseUnsignedLong(attributes.getValue("ticksPerQuarterNote"));

    this.timeConfiguration.setTicksPerQuarterNote(ticksPerQuarterNote);
  }

  @Override
  public OBTimeConfiguration onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.timeConfiguration.build();
  }
}
