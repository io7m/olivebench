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

import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.olivebench.model.OBDeleteableReadableType;
import com.io7m.olivebench.model.OBIdentifiableType;
import com.io7m.olivebench.model.names.OBName;
import com.io7m.olivebench.model.spaces.OBSpaceRegionType;
import io.reactivex.rxjava3.core.Observable;

public interface OBCompositionNodeType
  extends OBIdentifiableType, OBDeleteableReadableType
{
  OBCompositionNodeKind kind();

  PAreaL<OBSpaceRegionType> areaRelative();

  void setAreaRelative(PAreaL<OBSpaceRegionType> newArea);

  Observable<PAreaL<OBSpaceRegionType>> areaRelativeProperty();

  OBName name();

  Observable<OBName> nameProperty();

  void setName(OBName name);

  default void setName(
    final String name)
  {
    this.setName(OBName.of(name));
  }
}
