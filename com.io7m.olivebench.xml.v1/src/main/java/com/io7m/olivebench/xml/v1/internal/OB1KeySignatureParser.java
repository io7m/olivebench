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
import com.io7m.olivebench.composition.OBKeySignature;
import com.io7m.olivebench.composition.OBSignificantPitchClass;
import org.xml.sax.Attributes;

import java.util.Map;

import static com.io7m.olivebench.xml.v1.internal.OB1Names.element;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public final class OB1KeySignatureParser
  implements BTElementHandlerType<Object, OBKeySignature>
{
  private OBKeySignature.Builder keySignature;

  public OB1KeySignatureParser()
  {

  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return ofEntries(
      entry(
        element("SignificantPitchClass"),
        c -> new OB1SignificantPitchClassParser())
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof OBSignificantPitchClass) {
      this.keySignature.addSignificantPitchClasses(
        (OBSignificantPitchClass) result);
      return;
    }

    throw new IllegalStateException(
      String.format("Unrecognized value: %s", result)
    );
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.keySignature = OBKeySignature.builder();
    this.keySignature.setPitchClassOffset(
      Integer.parseInt(attributes.getValue("pitchClassOffset")));
    this.keySignature.setNotesPerOctave(
      Integer.parseInt(attributes.getValue("notesPerOctave")));
    this.keySignature.setName(
      attributes.getValue("name"));
  }

  @Override
  public OBKeySignature onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.keySignature.build();
  }
}
