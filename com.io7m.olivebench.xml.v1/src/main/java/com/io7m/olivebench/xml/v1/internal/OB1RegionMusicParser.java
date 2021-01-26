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
import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.composition.OBKeySignature;
import com.io7m.olivebench.composition.OBTimeSignature;
import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.regions.OBNote;
import com.io7m.olivebench.composition.regions.OBRegionMusicType;
import com.io7m.olivebench.composition.spaces.OBSpacePatternTrackType;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.io7m.olivebench.xml.v1.internal.OB1Names.element;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public final class OB1RegionMusicParser
  implements BTElementHandlerType<Object, OBRegionMusicType>
{
  private final OBTrackType track;
  private OBRegionMusicType region;

  public OB1RegionMusicParser(
    final OBTrackType inTrack)
  {
    this.track =
      Objects.requireNonNull(inTrack, "track");
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return ofEntries(
      entry(
        element("Area"),
        c -> new OB1AreaParser()),
      entry(
        element("Note"),
        c -> new OB1NoteParser()),
      entry(
        element("KeySignature"),
        c -> new OB1KeySignatureParser()),
      entry(
        element("TimeSignature"),
        c -> new OB1TimeSignatureParser())
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof OBNote) {
      this.region.addNote((OBNote) result);
      return;
    }
    if (result instanceof PAreaL) {
      this.region.setBounds((PAreaL<OBSpacePatternTrackType>) result);
      return;
    }
    if (result instanceof OBKeySignature) {
      this.region.setKeySignature((OBKeySignature) result);
      return;
    }
    if (result instanceof OBTimeSignature) {
      this.region.setTimeSignature((OBTimeSignature) result);
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
    this.region =
      this.track.createMusicRegion(
        UUID.fromString(attributes.getValue("id")),
        PAreaL.of(0L, 1L, 0L, 128L),
        OBTimeSignature.of(4, 4),
        OBKeySignature.builder()
          .setName("C Major")
          .setNotesPerOctave(12)
          .setPitchClassOffset(0)
          .build()
      );
  }

  @Override
  public OBRegionMusicType onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.region;
  }
}