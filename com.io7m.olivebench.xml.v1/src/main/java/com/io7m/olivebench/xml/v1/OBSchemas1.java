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
import java.util.List;

public final class OBSchemas1
{
  private static final URI NAMESPACE_1_URI =
    URI.create("urn:com.io7m.olivebench:xml:1");

  private static final JXESchemaDefinition SCHEMA_1 =
    JXESchemaDefinition.builder()
      .setFileIdentifier("composition-1.xsd")
      .setLocation(OBSchemas1.class.getResource(
        "/com/io7m/olivebench/xml/composition-1.xsd"))
      .setNamespace(NAMESPACE_1_URI)
      .build();

  private static final JXESchemaDefinition DC_SCHEMA =
    JXESchemaDefinition.builder()
      .setFileIdentifier("dc.xsd")
      .setLocation(OBSchemas1.class.getResource(
        "/com/io7m/olivebench/xml/dc.xsd"))
      .setNamespace(URI.create("http://purl.org/dc/elements/1.1/"))
      .build();

  private static final JXESchemaDefinition XML_SCHEMA =
    JXESchemaDefinition.builder()
      .setFileIdentifier("xml.xsd")
      .setLocation(OBSchemas1.class.getResource(
        "/com/io7m/olivebench/xml/xml.xsd"))
      .setNamespace(URI.create("http://www.w3.org/XML/1998/namespace"))
      .build();

  private OBSchemas1()
  {

  }

  /**
   * @return The 1.0 schema namespace URI.
   */

  public static URI namespace1()
  {
    return NAMESPACE_1_URI;
  }

  /**
   * @return The 1.0 schema.
   */

  public static JXESchemaDefinition schema1()
  {
    return SCHEMA_1;
  }

  /**
   * @return The 1.0 schemas.
   */

  public static List<JXESchemaDefinition> schemas1()
  {
    return List.of(XML_SCHEMA, DC_SCHEMA, SCHEMA_1);
  }
}
