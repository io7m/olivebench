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

package com.io7m.olivebench.controller.server;

import com.io7m.olivebench.controller.api.OBControllerType;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Objects;

public final class OBControllerClientInterpreter
  implements OBControllerClientInterpreterType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBControllerClientInterpreter.class);

  private final Context context;
  private final OBControllerClientAPIType api;

  public OBControllerClientInterpreter(
    final Context inContext,
    final OBControllerClientAPIType inApi)
  {
    this.context =
      Objects.requireNonNull(inContext, "context");
    this.api =
      Objects.requireNonNull(inApi, "api");
  }

  public static OBControllerClientInterpreter create(
    final OBControllerType inController,
    final OutputStream outputStream)
  {
    Objects.requireNonNull(inController, "inController");
    Objects.requireNonNull(outputStream, "outputStream");

    final var context =
      Context.newBuilder("js")
        .allowHostAccess(HostAccess.ALL)
        .allowHostClassLookup(OBControllerClientInterpreter::isClassAllowed)
        .out(outputStream)
        .option("js.ecmascript-version", "2020")
        .option("js.strict", "true")
        .build();

    final var api = new OBControllerClientAPI(inController);
    final var bindings = context.getBindings("js");
    bindings.putMember("olivebench", api);
    return new OBControllerClientInterpreter(context, api);
  }

  private static boolean isClassAllowed(
    final String name)
  {
    LOG.debug("checkClass: {}", name);

    if (Objects.equals(
      name, OBControllerClientAPI.class.getCanonicalName())) {
      return true;
    }
    return Objects.equals(
      name, OBControllerClientAPIType.class.getCanonicalName());
  }

  @Override
  public void interpret(
    final String line)
    throws Exception
  {
    LOG.trace("evaluate: {}", line);
    this.context.eval("js", line);
  }
}
