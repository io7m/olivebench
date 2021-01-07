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
import com.io7m.olivebench.model.properties.OBPropertyType;
import com.io7m.olivebench.model.spaces.OBSpaceRegionType;
import io.reactivex.rxjava3.core.Observable;

public interface OBCompositionNodeType
  extends OBIdentifiableType, OBDeleteableReadableType
{
  OBCompositionNodeKind kind();

  OBPropertyType<OBNodeMetadata> nodeMetadata();

  default void setNodeName(final String name)
  {
    this.nodeMetadata().update(meta -> meta.withName(OBName.of(name)));
  }

  default OBName nodeName()
  {
    return this.nodeMetadata().read().name();
  }

  default PAreaL<OBSpaceRegionType> nodeArea()
  {
    return this.nodeMetadata().read().area();
  }

  default void setNodeAreaRelative(final PAreaL<OBSpaceRegionType> area)
  {
    this.nodeMetadata().update(meta -> meta.withArea(area));
  }

  Observable<Object> changes();
}
