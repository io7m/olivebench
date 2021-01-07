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

package com.io7m.olivebench.model.graph;

import com.io7m.olivebench.model.properties.OBPropertyType;
import io.reactivex.rxjava3.core.Observable;

public interface OBRegionType<T> extends OBCompositionNodeType
{
  OBPropertyType<T> data();

  @Override
  default Observable<Object> changes()
  {
    final var nodeChanges =
      this.nodeMetadata().asObservable().ofType(Object.class);
    final var regionChanges =
      this.data().asObservable().ofType(Object.class);

    return nodeChanges.mergeWith(regionChanges);
  }

  interface OBSectionRegionType extends OBRegionType<OBSectionRegionData>
  {
    @Override
    default OBCompositionNodeKind kind()
    {
      return OBCompositionNodeKind.SECTION_REGION;
    }
  }

  interface OBCurveRegionType extends OBRegionType<Object>
  {
    @Override
    default OBCompositionNodeKind kind()
    {
      return OBCompositionNodeKind.CURVE_REGION;
    }
  }

  interface OBMusicRegionType<T> extends OBRegionType<T>
  {
    @Override
    default OBCompositionNodeKind kind()
    {
      return OBCompositionNodeKind.MUSIC_REGION;
    }

    interface OBNoteRegionType extends OBMusicRegionType<Object>
    {
      @Override
      default OBCompositionNodeKind kind()
      {
        return OBCompositionNodeKind.NOTE_REGION;
      }
    }
  }

  interface OBDecorativeRegionType<T> extends OBRegionType<T>
  {
    interface OBTextRegionType extends OBDecorativeRegionType<OBTextRegionData>
    {
      @Override
      default OBCompositionNodeKind kind()
      {
        return OBCompositionNodeKind.TEXT_REGION;
      }

      default String text()
      {
        return this.data().read().text();
      }

      default void setText(final String text)
      {
        this.data().update(data -> data.withText(text));
      }
    }

    interface OBImageRegionType extends OBDecorativeRegionType<Object>
    {
      @Override
      default OBCompositionNodeKind kind()
      {
        return OBCompositionNodeKind.IMAGE_REGION;
      }
    }
  }
}
