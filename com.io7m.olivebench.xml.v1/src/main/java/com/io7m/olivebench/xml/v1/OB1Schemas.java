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

package com.io7m.olivebench.xml.v1;

import com.io7m.jxe.core.JXESchemaDefinition;

import java.net.URI;

/**
 * Information about the 1.0 XML collection format.
 */

public final class OB1Schemas
{
  /**
   * The 1.0 schema namespace URI.
   */

  public static final URI NAMESPACE_1_URI =
    URI.create("urn:com.io7m.olivebench:xml:1");

  /**
   * The 1.0 schema.
   */

  public static final JXESchemaDefinition SCHEMA_1 =
    JXESchemaDefinition.builder()
      .setFileIdentifier("schema-1.xsd")
      .setLocation(OB1Schemas.class.getResource(
        "/com/io7m/olivebench/xml/v1/schema-1.xsd"))
      .setNamespace(NAMESPACE_1_URI)
      .build();

  private OB1Schemas()
  {

  }
}
