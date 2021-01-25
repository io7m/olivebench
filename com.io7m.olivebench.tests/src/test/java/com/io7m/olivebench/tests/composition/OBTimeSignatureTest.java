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

package com.io7m.olivebench.tests.composition;

import com.io7m.olivebench.composition.OBTimeSignature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class OBTimeSignatureTest
{
  @Test
  public void testToString()
  {
    assertEquals(
      OBTimeSignature.of(4, 4).toString(),
      OBTimeSignature.of(4, 4).toString()
    );
    assertNotEquals(
      OBTimeSignature.of(5, 4).toString(),
      OBTimeSignature.of(4, 4).toString()
    );
    assertEquals(
      OBTimeSignature.of(4, 1).toString(),
      OBTimeSignature.of(4, 1).toString()
    );
    assertEquals(
      OBTimeSignature.of(4, 2).toString(),
      OBTimeSignature.of(4, 2).toString()
    );
  }

  @Test
  public void test44()
  {
    final var timeSignature =
      OBTimeSignature.of(4, 4);
    assertEquals("4/4", timeSignature.show());
  }

  @Test
  public void testBadNumbers()
  {
    assertAll(
      () -> {
        assertThrows(
          IllegalArgumentException.class,
          () -> OBTimeSignature.of(4, 0));
      },
      () -> {
        assertThrows(
          IllegalArgumentException.class,
          () -> OBTimeSignature.of(0, 4));
      },
      () -> {
        assertThrows(
          IllegalArgumentException.class,
          () -> OBTimeSignature.of(4, 5));
      }
    );
  }
}
