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

import com.io7m.olivebench.composition_parser.spi.OBCompositionSPIParsersType;
import com.io7m.olivebench.composition_serializer.spi.OBCompositionSPISerializersType;
import com.io7m.olivebench.xml.v1.OB1Parsers;
import com.io7m.olivebench.xml.v1.OB1Serializers;

/**
 * Olivebench (XML 1.0 parser)
 */

module com.io7m.olivebench.xml.v1
{
  requires static org.immutables.value;
  requires static com.io7m.immutables.style;
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires com.io7m.blackthorne.api;
  requires com.io7m.jaffirm.core;
  requires com.io7m.jregions.core;
  requires com.io7m.jtensors.core;
  requires com.io7m.junreachable.core;
  requires com.io7m.jxe.core;
  requires com.io7m.olivebench.composition.serializer.spi;
  requires com.io7m.olivebench.composition_parser.spi;
  requires com.io7m.olivebench.exceptions;
  requires com.io7m.olivebench.model;
  requires com.io7m.olivebench.strings;
  requires org.jgrapht.core;
  requires org.slf4j;

  provides OBCompositionSPIParsersType with OB1Parsers;
  provides OBCompositionSPISerializersType with OB1Serializers;

  exports com.io7m.olivebench.xml.v1;
}
