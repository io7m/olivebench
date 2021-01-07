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

package com.io7m.olivebench.main;

import com.io7m.jade.api.ApplicationDirectories;
import com.io7m.jade.spi.ApplicationDirectoryConfiguration;
import com.io7m.olivebench.composition_parser.api.OBCompositionParsers;
import com.io7m.olivebench.composition_parser.api.OBCompositionParsersType;
import com.io7m.olivebench.composition_serializer.api.OBCompositionSerializers;
import com.io7m.olivebench.composition_serializer.api.OBCompositionSerializersType;
import com.io7m.olivebench.controller.OBController;
import com.io7m.olivebench.controller.OBControllerType;
import com.io7m.olivebench.preferences.OBPreferencesController;
import com.io7m.olivebench.preferences.OBPreferencesControllerType;
import com.io7m.olivebench.services.api.OBServiceDirectory;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.strings.OBStrings;
import com.io7m.olivebench.strings.OBStringsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MainServices
{
  private static final Logger LOG = LoggerFactory.getLogger(MainServices.class);

  private MainServices()
  {

  }

  private static OBPreferencesControllerType startPreferencesController()
  {
    final var configuration =
      ApplicationDirectoryConfiguration.builder()
        .setApplicationName("com.io7m.olivebench")
        .setPortablePropertyName("com.io7m.olivebench.portable")
        .build();

    final var directories =
      ApplicationDirectories.get(configuration);
    final var configurationDirectory =
      directories.configurationDirectory();
    final var configurationFile =
      configurationDirectory.resolve("preferences.xml");
    final var configurationFileTmp =
      configurationDirectory.resolve("preferences.xml.tmp");

    LOG.info("preferences: {}", configurationFile);
    return OBPreferencesController.create(
      configurationFile,
      configurationFileTmp
    );
  }

  public static OBServiceDirectoryType create()
  {
    final var services = new OBServiceDirectory();
    final var strings = OBStrings.of(OBStrings.getResourceBundle());
    services.register(OBStringsType.class, strings);
    final var preferencesController = startPreferencesController();
    services.register(OBPreferencesControllerType.class, preferencesController);
    final var parsers = OBCompositionParsers.create();
    services.register(OBCompositionParsersType.class, parsers);
    final var serializers = OBCompositionSerializers.create();
    services.register(OBCompositionSerializersType.class, serializers);
    final var controller = OBController.create(services);
    services.register(OBControllerType.class, controller);
    return services;
  }
}
