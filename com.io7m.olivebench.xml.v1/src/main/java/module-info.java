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

import com.io7m.olivebench.composition.parser.spi.OBCompositionSPIParsersType;
import com.io7m.olivebench.composition.serializer.spi.OBCompositionSPISerializersType;
import com.io7m.olivebench.xml.v1.OBCompositionParserV1;
import com.io7m.olivebench.xml.v1.OBCompositionSerializerV1;

/**
 * Olivebench (XML V1 support)
 */

module com.io7m.olivebench.xml.v1
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;
  requires static org.immutables.value;

  requires transitive com.io7m.olivebench.composition.parser.spi;
  requires com.io7m.olivebench.composition.serializer.spi;

  provides OBCompositionSPIParsersType with OBCompositionParserV1;
  provides OBCompositionSPISerializersType with OBCompositionSerializerV1;

  exports com.io7m.olivebench.xml.v1;
}
