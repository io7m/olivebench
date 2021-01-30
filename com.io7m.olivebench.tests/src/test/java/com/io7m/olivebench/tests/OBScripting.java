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

package com.io7m.olivebench.tests;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;

import java.util.Objects;

public final class OBScripting
{
  private OBScripting()
  {

  }

  public static void main(
    final String[] args)
  {
    final var context =
      Context.newBuilder("js")
        .allowHostAccess(HostAccess.ALL)
        .allowHostClassLookup(className -> Objects.equals(className, OBScripting.class.getCanonicalName()))
        .build();

    final var bindings = context.getBindings("js");
    bindings.putMember("x", new OBScripting());

    context.eval("js", "x.run('x')");
    context.eval("js", "x.run()");
  }

  public void run(final String x)
  {
    System.out.println("doSomething!");
  }
}
