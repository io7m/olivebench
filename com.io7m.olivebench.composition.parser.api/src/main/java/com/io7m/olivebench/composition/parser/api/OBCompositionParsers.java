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

package com.io7m.olivebench.composition.parser.api;

import com.io7m.blackthorne.api.BTElementHandlerConstructorType;
import com.io7m.blackthorne.api.BTException;
import com.io7m.blackthorne.api.BTParseError;
import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.blackthorne.jxe.BlackthorneJXE;
import com.io7m.jxe.core.JXESchemaResolutionMappings;
import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.composition.parser.spi.OBCompositionSPIParsersType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class OBCompositionParsers implements OBCompositionParsersType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBCompositionParsersType.class);

  public OBCompositionParsers()
  {

  }

  @Override
  public OBCompositionParserType createParser(
    final OBServiceDirectoryType services,
    final URI source,
    final InputStream stream)
    throws UnsupportedOperationException
  {
    final var parsers =
      services.optionalServices(OBCompositionSPIParsersType.class);

    if (parsers.isEmpty()) {
      throw new UnsupportedOperationException();
    }

    final var locale =
      Locale.getDefault();
    final var clock =
      Clock.systemUTC();

    final var rootElements =
      new HashMap<BTQualifiedName, BTElementHandlerConstructorType<?, OBCompositionType>>(
        parsers.size());

    final var schemaMappings =
      JXESchemaResolutionMappings.builder();

    for (final var parser : parsers) {
      final var schemas = parser.schemas();
      for (final var schema : schemas) {
        schemaMappings.putMappings(schema.namespace(), schema);
      }
      rootElements.put(
        parser.rootName(),
        c -> parser.createHandler(clock, locale, services));
    }

    return new Parser(source, stream, rootElements, schemaMappings.build());
  }

  private static final class Parser implements OBCompositionParserType
  {
    private final URI source;
    private final InputStream stream;
    private final Map<BTQualifiedName, BTElementHandlerConstructorType<?, OBCompositionType>> rootElements;
    private final JXESchemaResolutionMappings mappings;
    private final List<OBCompositionParserError> errors;

    Parser(
      final URI inSource,
      final InputStream inStream,
      final Map<BTQualifiedName, BTElementHandlerConstructorType<?, OBCompositionType>> inRoots,
      final JXESchemaResolutionMappings inMappings)
    {
      this.source =
        Objects.requireNonNull(inSource, "source");
      this.stream =
        Objects.requireNonNull(inStream, "stream");
      this.rootElements =
        Objects.requireNonNull(inRoots, "rootElements");
      this.mappings =
        Objects.requireNonNull(inMappings, "mappings");
      this.errors =
        new ArrayList<>();
    }

    private static OBCompositionParserError ofBTError(
      final BTParseError error)
    {
      final var lexical = error.lexical();

      switch (error.severity()) {
        case WARNING: {
          LOG.warn(
            "{}:{}: {}",
            Integer.valueOf(lexical.line()),
            Integer.valueOf(lexical.column()),
            error.message()
          );
          break;
        }
        case ERROR: {
          LOG.error(
            "{}:{}: {}",
            Integer.valueOf(lexical.line()),
            Integer.valueOf(lexical.column()),
            error.message()
          );
          break;
        }
      }

      return OBCompositionParserError.builder()
        .setLexical(error.lexical())
        .setMessage(error.message())
        .setException(error.exception())
        .build();
    }

    @Override
    public List<OBCompositionParserError> errors()
    {
      return Collections.unmodifiableList(this.errors);
    }

    @Override
    public Optional<OBCompositionType> execute()
    {
      try {
        return Optional.of(BlackthorneJXE.parse(
          this.source,
          this.stream,
          this.rootElements,
          this.mappings
        ));
      } catch (final BTException e) {
        this.errors.addAll(
          e.errors()
            .stream()
            .map(Parser::ofBTError)
            .collect(Collectors.toList())
        );
        return Optional.empty();
      }
    }

    @Override
    public void close()
      throws IOException
    {
      this.stream.close();
    }
  }
}
