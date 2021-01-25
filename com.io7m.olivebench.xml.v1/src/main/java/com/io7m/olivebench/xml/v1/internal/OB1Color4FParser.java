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
import com.io7m.jtensors.core.unparameterized.vectors.Vector4D;
import org.xml.sax.Attributes;

public final class OB1Color4FParser
  implements BTElementHandlerType<Object, Vector4D>
{
  private double red;
  private double green;
  private double blue;
  private double alpha;

  public OB1Color4FParser()
  {

  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.red =
      Double.parseDouble(attributes.getValue("red"));
    this.green =
      Double.parseDouble(attributes.getValue("green"));
    this.blue =
      Double.parseDouble(attributes.getValue("blue"));
    this.alpha =
      Double.parseDouble(attributes.getValue("alpha"));
  }

  @Override
  public Vector4D onElementFinished(
    final BTElementParsingContextType context)
  {
    return Vector4D.of(this.red, this.green, this.blue, this.alpha);
  }
}
