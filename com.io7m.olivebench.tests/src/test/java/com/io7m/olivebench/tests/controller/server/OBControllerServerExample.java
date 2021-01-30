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

package com.io7m.olivebench.tests.controller.server;

import com.io7m.olivebench.controller.OBController;
import com.io7m.olivebench.controller.server.OBControllerClientInterpreters;
import com.io7m.olivebench.controller.server.OBControllerServers;
import com.io7m.olivebench.services.api.OBServiceDirectory;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public final class OBControllerServerExample
{
  private OBControllerServerExample()
  {

  }

  public static void main(
    final String[] args)
    throws Throwable
  {
    final var services =
      new OBServiceDirectory();
    final var controller =
      OBController.create(Clock.systemUTC(), services, Locale.ENGLISH);
    final var interpreters =
      new OBControllerClientInterpreters(controller);
    final var servers =
      new OBControllerServers();

    final var interpreter =
      interpreters.create(ByteArrayOutputStream.nullOutputStream());

    try (var server =
           servers.create(
             interpreters,
             new InetSocketAddress("localhost", 9000))) {

      try {
        server.start().get();
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (final ExecutionException e) {
        throw e.getCause();
      }

      while (true) {
        try {
          Thread.sleep(1_000L);
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
