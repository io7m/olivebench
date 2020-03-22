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

package com.io7m.olivebench.composition_parser.api;

import com.io7m.blackthorne.api.BTContentHandler;
import com.io7m.blackthorne.api.BTParseError;
import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jlexing.core.LexicalPositions;
import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.jxe.core.JXESchemaResolutionMappings;
import com.io7m.jxe.core.JXEXInclude;
import com.io7m.olivebench.composition_parser.spi.OBCompositionSPIParsersType;
import com.io7m.olivebench.model.OBCompositionType;
import com.io7m.olivebench.strings.OBStringsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The default implementation of the parser API.
 */

public final class OBCompositionParsers implements OBCompositionParsersType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBCompositionParsers.class);

  private static final JXEHardenedSAXParsers PARSERS =
    new JXEHardenedSAXParsers();

  private final List<OBCompositionSPIParsersType> parsers;

  private OBCompositionParsers(
    final List<OBCompositionSPIParsersType> inParsers)
  {
    this.parsers = inParsers;
  }

  /**
   * Construct a new API using any parser implementation that can be found
   * via {@link ServiceLoader}.
   *
   * @return A parser API
   */

  public static OBCompositionParsersType create()
  {
    final var loader =
      ServiceLoader.load(OBCompositionSPIParsersType.class);
    final var iterator =
      loader.spliterator();
    final var parsers =
      StreamSupport.stream(iterator, false)
        .collect(Collectors.toList());

    LOG.debug("{} parsers available", Integer.valueOf(parsers.size()));
    for (int index = 0; index < parsers.size(); ++index) {
      final var parser = parsers.get(index);
      LOG.debug("[{}] {}", Integer.valueOf(index), parser);
    }

    return createWith(parsers);
  }

  /**
   * Construct a new API using the given parsers.
   *
   * @param parsers The set of available parsers
   *
   * @return A parser API
   */

  public static OBCompositionParsersType createWith(
    final List<OBCompositionSPIParsersType> parsers)
  {
    return new OBCompositionParsers(parsers);
  }

  private static OBCompositionParserError errorBrokenXMLParser(
    final URI uri,
    final ParserConfigurationException e)
  {
    return OBCompositionParserError.builder()
      .setMessage(e.getLocalizedMessage())
      .setLexical(lexicalOf(uri, e))
      .setException(e)
      .build();
  }

  private static OBCompositionParserError errorMalformedXML(
    final URI uri,
    final SAXException e)
  {
    return OBCompositionParserError.builder()
      .setLexical(lexicalOf(uri, e))
      .setMessage(e.getLocalizedMessage())
      .setException(e)
      .build();
  }

  private static LexicalPosition<URI> lexicalOf(
    final URI uri,
    final Exception e)
  {
    if (e instanceof SAXParseException) {
      final var parseEx = (SAXParseException) e;
      return LexicalPosition.<URI>builder()
        .setColumn(parseEx.getColumnNumber())
        .setLine(parseEx.getLineNumber())
        .setFile(uri)
        .build();
    }
    return LexicalPositions.zeroWithFile(uri);
  }

  private static OBCompositionParserError blackthorneToRV(
    final BTParseError error)
  {
    return OBCompositionParserError.builder()
      .setLexical(error.lexical())
      .setMessage(error.message())
      .setException(error.exception())
      .build();
  }

  private static OBCompositionParserError errorIO(
    final URI uri,
    final Exception e)
  {
    return OBCompositionParserError.builder()
      .setLexical(lexicalOf(uri, e))
      .setMessage(e.getLocalizedMessage())
      .setException(e)
      .build();
  }

  @Override
  public OBCompositionParserType createParser(
    final OBStringsType strings,
    final URI source,
    final InputStream stream)
  {
    return new Parser(this.parsers, strings, source, stream);
  }

  private static final class Parser implements OBCompositionParserType
  {
    private final List<OBCompositionSPIParsersType> providers;
    private final OBStringsType strings;
    private final URI source;
    private final InputStream stream;
    private final ArrayList<OBCompositionParserError> errors;

    private Parser(
      final List<OBCompositionSPIParsersType> inProviders,
      final OBStringsType inStrings,
      final URI inSource,
      final InputStream inStream)
    {
      this.providers =
        Objects.requireNonNull(inProviders, "inProviders");
      this.strings =
        Objects.requireNonNull(inStrings, "strings");
      this.source =
        Objects.requireNonNull(inSource, "inSource");
      this.stream =
        Objects.requireNonNull(inStream, "inStream");
      this.errors = new ArrayList<>();
    }

    @Override
    public List<OBCompositionParserError> errors()
    {
      return List.copyOf(this.errors);
    }

    @Override
    public Optional<OBCompositionType> execute()
    {
      try {
        final var contentHandlerBuilder =
          BTContentHandler.<OBCompositionType>builder();
        final var mappingsBuilder =
          JXESchemaResolutionMappings.builder();

        LOG.debug(
          "{} providers available",
          Integer.valueOf(this.providers.size()));

        for (final var candidate : this.providers) {
          final var schema = candidate.schema();
          final var schemaNamespace = schema.namespace();
          mappingsBuilder.putMappings(schemaNamespace, schema);

          LOG.debug("adding schema {}", schemaNamespace);
          contentHandlerBuilder.addHandler(
            BTQualifiedName.of(schemaNamespace.toString(), "Composition"),
            context -> candidate.createHandler(this.strings)
          );
        }

        final var inputSource = new InputSource(this.stream);
        final var urlText = this.source.toString();
        inputSource.setPublicId(urlText);

        final var contentHandler =
          contentHandlerBuilder.build(
            this.source,
            parseError -> this.errors.add(blackthorneToRV(parseError)));

        final var parser =
          PARSERS.createXMLReader(
            Optional.empty(),
            JXEXInclude.XINCLUDE_DISABLED,
            mappingsBuilder.build());

        parser.setErrorHandler(contentHandler);
        parser.setContentHandler(contentHandler);

        final var timeThen = Instant.now();
        parser.parse(inputSource);
        final var timeNow = Instant.now();
        LOG.debug(
          "parsed collection in {}",
          Duration.between(timeThen, timeNow));

        return contentHandler.result().map(Function.identity());
      } catch (final ParserConfigurationException e) {
        this.errors.add(errorBrokenXMLParser(this.source, e));
        return Optional.empty();
      } catch (final SAXException e) {
        this.errors.add(errorMalformedXML(this.source, e));
        return Optional.empty();
      } catch (final Exception e) {
        this.errors.add(errorIO(this.source, e));
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
