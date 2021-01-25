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
import com.io7m.olivebench.composition.OBTrackMetadata;
import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.composition.regions.OBRegionType;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.io7m.olivebench.xml.v1.internal.OB1Names.element;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public final class OB1TrackParser
  implements BTElementHandlerType<Object, OBTrackType>
{
  private final OBCompositionType composition;
  private OBTrackType track;

  public OB1TrackParser(
    final OBCompositionType inComposition)
  {
    this.composition =
      Objects.requireNonNull(inComposition, "composition");
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return ofEntries(
      entry(element("TrackMetadata"), c -> new OB1TrackMetadataParser()),
      entry(element("RegionMusic"), c -> new OB1RegionMusicParser(this.track)),
      entry(element("RegionText"), c -> new OB1RegionTextParser(this.track))
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof OBTrackMetadata) {
      this.track.setMetadata((OBTrackMetadata) result);
      return;
    }
    if (result instanceof OBRegionType) {
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
    this.track =
      this.composition.createTrack(
        UUID.fromString(attributes.getValue("id"))
      );
  }

  @Override
  public OBTrackType onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.track;
  }
}
