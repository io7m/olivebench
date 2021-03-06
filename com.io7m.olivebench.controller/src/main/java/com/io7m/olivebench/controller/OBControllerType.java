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

import com.io7m.olivebench.model.OBCompositionReadableType;
import com.io7m.olivebench.model.graph.OBChannelMetadata;
import com.io7m.olivebench.model.metadata.OBCompositionMetadata;
import com.io7m.olivebench.model.names.OBName;
import com.io7m.olivebench.services.api.OBServiceType;
import com.io7m.olivebench.strings.OBStringsType;
import io.reactivex.rxjava3.core.Observable;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface OBControllerType extends Closeable, OBServiceType
{
  CompletableFuture<?> newComposition();

  CompletableFuture<?> closeComposition();

  CompletableFuture<?> createChannel(OBName name);

  CompletableFuture<?> openComposition(Path file);

  CompletableFuture<?> saveComposition();

  CompletableFuture<?> saveAsComposition(Path file);

  Optional<Path> currentFilename();

  boolean unsavedChanges();

  CompletableFuture<?> undo();

  OBStringsType strings();

  Observable<OBControllerEventType> events();

  Optional<OBCompositionReadableType> compositionSnapshot();

  CompletableFuture<?> updateChannelMetadata(
    UUID channelId,
    Function<OBChannelMetadata, OBChannelMetadata> updater
  );

  CompletableFuture<?> updateMetadata(
    Function<OBCompositionMetadata, OBCompositionMetadata> updater
  );
}
