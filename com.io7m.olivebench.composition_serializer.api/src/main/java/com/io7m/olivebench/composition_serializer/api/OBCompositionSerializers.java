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

package com.io7m.olivebench.composition_serializer.api;

import com.io7m.olivebench.composition_serializer.spi.OBCompositionSPISerializerType;
import com.io7m.olivebench.composition_serializer.spi.OBCompositionSPISerializersType;
import com.io7m.olivebench.model.OBCompositionReadableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class OBCompositionSerializers implements
  OBCompositionSerializersType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBCompositionSerializers.class);

  private final List<OBCompositionSPISerializersType> serializers;

  private OBCompositionSerializers(
    final List<OBCompositionSPISerializersType> inSerializers)
  {
    this.serializers = inSerializers;
  }

  public static OBCompositionSerializersType create()
  {
    return createWith(fromServiceLoader());
  }

  public static OBCompositionSerializersType createWith(
    final List<OBCompositionSPISerializersType> serializers)
  {
    return new OBCompositionSerializers(serializers);
  }

  private static List<OBCompositionSPISerializersType> fromServiceLoader()
  {
    final var loader =
      ServiceLoader.load(OBCompositionSPISerializersType.class);
    final var iterator =
      loader.spliterator();
    final var serializers =
      StreamSupport.stream(iterator, false)
        .collect(Collectors.toList());

    LOG.debug("{} serializers available", Integer.valueOf(serializers.size()));
    for (int index = 0; index < serializers.size(); ++index) {
      final var serializer = serializers.get(index);
      LOG.debug("[{}] {}", Integer.valueOf(index), serializer);
    }

    return serializers;
  }

  @Override
  public OBCompositionSerializerType createSerializer(
    final URI target,
    final OutputStream stream,
    final OBCompositionReadableType composition)
  {
    final var matchingOpt =
      this.serializers.stream()
        .max(
          Comparator.comparingInt(OBCompositionSPISerializersType::versionMajor)
            .thenComparingInt(OBCompositionSPISerializersType::versionMinor));

    if (matchingOpt.isEmpty()) {
      throw new UnsupportedOperationException("No serializer is available");
    }

    final var matching = matchingOpt.get();
    return new Serializer(matching.create(target, stream, composition));
  }

  @Override
  public OBCompositionSerializerType createSerializer(
    final int versionMajor,
    final int versionMinor,
    final URI target,
    final OutputStream stream,
    final OBCompositionReadableType composition)
  {
    final var matchingOpt =
      this.serializers.stream()
        .filter(candidate -> candidate.versionMajor() == versionMajor
          && candidate.versionMinor() == versionMinor).findFirst();

    if (matchingOpt.isEmpty()) {
      throw new UnsupportedOperationException(
        String.format(
          "No serializer is available supporting version %d.%d",
          Integer.valueOf(versionMajor),
          Integer.valueOf(versionMinor)));
    }

    final var matching = matchingOpt.get();
    return new Serializer(matching.create(target, stream, composition));
  }

  private static final class Serializer implements OBCompositionSerializerType
  {
    private final OBCompositionSPISerializerType serializer;

    Serializer(
      final OBCompositionSPISerializerType inSerializer)
    {
      this.serializer = inSerializer;
    }

    @Override
    public void execute()
      throws Exception
    {
      final var timeThen = Instant.now();
      this.serializer.execute();
      final var timeNow = Instant.now();
      LOG.debug("wrote composition in {}", Duration.between(timeThen, timeNow));
    }

    @Override
    public void close()
      throws IOException
    {
      this.serializer.close();
    }
  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBCompositionSerializers 0x%s]",
      Integer.toUnsignedString(System.identityHashCode(this), 16)
    );
  }
}
