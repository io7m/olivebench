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
import com.io7m.blackthorne.api.Blackthorne;
import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.regions.OBRegionTextType;
import com.io7m.olivebench.composition.spaces.OBSpacePatternTrackType;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.io7m.olivebench.xml.v1.internal.OB1Names.element;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public final class OB1RegionTextParser
  implements BTElementHandlerType<Object, OBRegionTextType>
{
  private final OBTrackType track;
  private OBRegionTextType region;

  public OB1RegionTextParser(
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
      entry(element("Area"), c -> new OB1AreaParser()),
      entry(element("Text"), Blackthorne.forScalarString(element("Text")))
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof PAreaL) {
      this.region.setBounds((PAreaL<OBSpacePatternTrackType>) result);
      return;
    }
    if (result instanceof String) {
      this.region.setText((String) result);
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
      this.track.createTextRegion(
        UUID.fromString(attributes.getValue("id")),
        PAreaL.of(0L, 1L, 0L, 128L)
      );
  }

  @Override
  public OBRegionTextType onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.region;
  }
}
