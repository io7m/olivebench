/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.olivebench.xml.v1;

import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.composition.serializer.spi.OBCompositionSPISerializerType;
import com.io7m.olivebench.composition.serializer.spi.OBCompositionSPISerializersType;
import com.io7m.olivebench.xml.v1.internal.OB1CompositionSerializer;

import java.io.OutputStream;
import java.net.URI;

public final class OBCompositionSerializerV1
  implements OBCompositionSPISerializersType
{
  public OBCompositionSerializerV1()
  {

  }

  @Override
  public int versionMajor()
  {
    return 1;
  }

  @Override
  public int versionMinor()
  {
    return 0;
  }

  @Override
  public OBCompositionSPISerializerType create(
    final URI target,
    final OutputStream output,
    final OBCompositionType composition)
  {
    return new OB1CompositionSerializer(target, output, composition);
  }
}
