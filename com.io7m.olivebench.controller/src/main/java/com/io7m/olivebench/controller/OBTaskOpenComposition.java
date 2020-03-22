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

package com.io7m.olivebench.controller;

import com.io7m.olivebench.composition_parser.api.OBCompositionParserType;
import com.io7m.olivebench.composition_parser.api.OBCompositionParsersType;
import com.io7m.olivebench.preferences.OBPreferencesControllerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.OptionalDouble;

public final class OBTaskOpenComposition implements OBControllerTaskType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBTaskOpenComposition.class);

  private final OBController controller;
  private final Path file;
  private final OBCompositionParsersType parsers;
  private final OBPreferencesControllerType preferences;

  public OBTaskOpenComposition(
    final OBController inController,
    final OBCompositionParsersType inParsers,
    final OBPreferencesControllerType inPreferences,
    final Path inFile)
  {
    this.controller =
      Objects.requireNonNull(inController, "inController");
    this.parsers =
      Objects.requireNonNull(inParsers, "inParsers");
    this.preferences =
      Objects.requireNonNull(inPreferences, "inPreferences");
    this.file =
      Objects.requireNonNull(inFile, "file");
  }

  @Override
  public String name()
  {
    return this.controller.strings().controllerOpenComposition();
  }

  @Override
  public void taskDo()
    throws OBTaskFailureException
  {
    final var strings =
      this.controller.strings();

    this.controller.publishEvent(
      OBControllerEventTaskProgressChanged.of(
        this.name(),
        strings.controllerOpenCompositionParsing(this.file),
        OptionalDouble.empty(),
        OptionalDouble.empty()
      ));

    try (var stream = Files.newInputStream(this.file)) {
      try (var parser =
             this.parsers.createParser(strings, this.file.toUri(), stream)) {
        final var compositionOpt = parser.execute();
        if (!parser.errors().isEmpty()) {
          throw this.publishParseErrors(parser);
        }

        final var composition = compositionOpt.get();
        composition.setFileName(this.file);
        this.controller.setComposition(composition);

        this.preferences.updateQuietly(prefs -> {
          prefs.recentItems().addRecentFile(this.file);
        });
      }
    } catch (final IOException e) {
      LOG.error("i/o error: ", e);
      throw new OBTaskFailureException(
        e,
        OBControllerEventTaskFailed.builder()
          .setTitle(strings.controllerOpenCompositionFailed())
          .setMessage(e.getMessage())
          .setException(e)
          .build()
      );
    }
  }

  private OBTaskFailureException publishParseErrors(
    final OBCompositionParserType parser)
  {
    final var builder = new StringBuilder(1024);
    for (final var error : parser.errors()) {
      final var lexical = error.lexical();

      LOG.error(
        "parse: {}: {}:{}: {} ",
        lexical.file().orElse(URI.create("urn:unspecified")),
        Integer.valueOf(lexical.line()),
        Integer.valueOf(lexical.column()),
        error.message(),
        error.exception().orElse(new Exception()));

      builder.append(lexical.file().map(URI::toString).orElse(""));
      builder.append(": ");
      builder.append(lexical.line());
      builder.append(":");
      builder.append(lexical.column());
      builder.append(": ");
      builder.append(error.message());

      final var exceptionOpt = error.exception();
      if (exceptionOpt.isPresent()) {
        final var exception = exceptionOpt.get();
        try (var sw = new StringWriter()) {
          try (var pw = new PrintWriter(sw)) {
            exception.printStackTrace(pw);
            pw.flush();
          }
          builder.append(sw.toString());
        } catch (final IOException e) {
          throw new UncheckedIOException(e);
        }
      }
    }

    return new OBTaskFailureException(
      OBControllerEventTaskFailed.builder()
        .setTitle(this.controller.strings().controllerOpenCompositionFailed())
        .setMessage(builder.toString())
        .build()
    );
  }

  @Override
  public UndoStyle undoStyle()
  {
    return UndoStyle.CLEARS_UNDO_STACK;
  }

  @Override
  public boolean isLongRunning()
  {
    return true;
  }

  @Override
  public void taskUndo()
  {
    throw new UnsupportedOperationException();
  }
}
