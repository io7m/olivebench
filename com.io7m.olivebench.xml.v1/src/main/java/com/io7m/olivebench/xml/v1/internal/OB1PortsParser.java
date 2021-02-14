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
import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.composition.ports.OBPortInputType;
import com.io7m.olivebench.composition.ports.OBPortOutputType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.io7m.olivebench.xml.v1.internal.OB1Names.element;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public final class OB1PortsParser
  implements BTElementHandlerType<Object, OB1Ports>
{
  private final OBCompositionType composition;
  private final List<OBPortInputType> portsInput;
  private final List<OBPortOutputType> portsOutput;

  public OB1PortsParser(
    final OBCompositionType inComposition)
  {
    this.composition =
      Objects.requireNonNull(inComposition, "composition");
    this.portsInput =
      new ArrayList<>();
    this.portsOutput =
      new ArrayList<>();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return ofEntries(
      entry(
        element("PortOutput"),
        c -> new OB1PortOutputParser(this.composition)),
      entry(
        element("PortInput"),
        c -> new OB1PortInputParser(this.composition))
    );
  }

  @Override
  public OB1Ports onElementFinished(
    final BTElementParsingContextType context)
  {
    return OB1Ports.of(
      List.copyOf(this.portsInput),
      List.copyOf(this.portsOutput)
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof OBPortInputType) {
      this.portsInput.add((OBPortInputType) result);
      return;
    }
    if (result instanceof OBPortOutputType) {
      this.portsOutput.add((OBPortOutputType) result);
      return;
    }
    throw new IllegalStateException(
      String.format("Unrecognized value: %s", result)
    );
  }
}
