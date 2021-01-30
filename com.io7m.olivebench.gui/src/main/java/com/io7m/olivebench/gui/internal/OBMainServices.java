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

package com.io7m.olivebench.gui.internal;

import com.io7m.olivebench.composition.OBCompositionFactoryType;
import com.io7m.olivebench.composition.OBCompositions;
import com.io7m.olivebench.composition.parser.api.OBCompositionParsers;
import com.io7m.olivebench.composition.parser.api.OBCompositionParsersType;
import com.io7m.olivebench.composition.parser.spi.OBCompositionSPIParsersType;
import com.io7m.olivebench.composition.serializer.api.OBCompositionSerializers;
import com.io7m.olivebench.composition.serializer.api.OBCompositionSerializersType;
import com.io7m.olivebench.composition.serializer.spi.OBCompositionSPISerializersType;
import com.io7m.olivebench.controller.OBController;
import com.io7m.olivebench.controller.api.OBControllerAsynchronousDecorator;
import com.io7m.olivebench.controller.api.OBControllerAsynchronousType;
import com.io7m.olivebench.controller.server.OBControllerClientInterpreters;
import com.io7m.olivebench.controller.server.OBControllerServerType;
import com.io7m.olivebench.controller.server.OBControllerServers;
import com.io7m.olivebench.services.api.OBServiceDirectory;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.services.api.OBServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.util.Locale;
import java.util.ServiceLoader;

public final class OBMainServices
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBMainServices.class);

  private OBMainServices()
  {

  }

  public static OBServiceDirectoryType create()
    throws Exception
  {
    final var locale =
      Locale.getDefault();
    final var services =
      new OBServiceDirectory();
    final var strings =
      new OBMainStrings(locale);
    final var controller =
      OBController.create(Clock.systemUTC(), services, locale);
    final var controllerAsync =
      OBControllerAsynchronousDecorator.create(controller);

    loadFromServiceLoader(services, OBCompositionSPISerializersType.class);
    loadFromServiceLoader(services, OBCompositionSPIParsersType.class);

    services.register(OBCompositionFactoryType.class, new OBCompositions());
    services.register(
      OBCompositionParsersType.class,
      new OBCompositionParsers());
    services.register(
      OBCompositionSerializersType.class,
      new OBCompositionSerializers());
    services.register(OBControllerAsynchronousType.class, controllerAsync);
    services.register(OBMainStrings.class, strings);

    createServer(services, controllerAsync);
    return services;
  }

  private static void createServer(
    final OBServiceDirectory services,
    final OBControllerAsynchronousType controller)
    throws IOException
  {
    final var interpreters =
      new OBControllerClientInterpreters(controller);
    final var servers =
      new OBControllerServers();
    final var server =
      servers.create(
        interpreters,
        new InetSocketAddress("localhost", 9700)
      );
    server.start();
    services.register(OBControllerServerType.class, server);
  }

  private static <T extends OBServiceType> void loadFromServiceLoader(
    final OBServiceDirectory services,
    final Class<T> clazz)
  {
    ServiceLoader.load(clazz)
      .stream()
      .map(ServiceLoader.Provider::get)
      .forEach(service -> services.register(clazz, service));
  }
}
