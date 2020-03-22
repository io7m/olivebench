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

package com.io7m.olivebench.tests;

import com.io7m.olivebench.model.names.OBName;
import com.io7m.olivebench.model.names.OBNames;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.quicktheories.QuickTheory;
import org.quicktheories.generators.SourceDSL;

import java.util.List;
import java.util.stream.Stream;

public class OBNamesTest
{
  @TestFactory
  public Stream<DynamicTest> testNamesValid()
  {
    return List.of(
      "0",
      "a",
      "_",
      "AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAA")
      .stream()
      .map(text -> DynamicTest.dynamicTest(
        "testValid_" + text,
        () -> OBNames.checkValid(text)));
  }

  @TestFactory
  public Stream<DynamicTest> testNamesInvalid()
  {
    return List.of(
      "AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_AAAAAAAAA_")
      .stream()
      .map(text -> DynamicTest.dynamicTest("testInvalid_" + text, () -> {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          OBNames.checkValid(text);
        });
      }));
  }

  @Test
  public void testOf()
  {
    QuickTheory.qt()
      .forAll(
        SourceDSL.strings().betweenCodePoints(0x41, 0x5a).ofLengthBetween(
          1,
          128),
        SourceDSL.strings().betweenCodePoints(0x41, 0x5a).ofLengthBetween(
          1,
          128))
      .check((s0, s1) -> {
        if (s0.equals(s1)) {
          return OBName.of(s0).equals(OBName.of(s1));
        } else {
          return !OBName.of(s0).equals(OBName.of(s1));
        }
      });
  }

  @Test
  public void testWithValue()
  {
    QuickTheory.qt()
      .forAll(
        SourceDSL.strings().betweenCodePoints(0x41, 0x5a).ofLengthBetween(
          1,
          128),
        SourceDSL.strings().betweenCodePoints(0x41, 0x5a).ofLengthBetween(
          1,
          128))
      .check((s0, s1) -> {
        final var c0 = OBName.of(s0);
        final var c1 = OBName.of(s1);
        if (c0.equals(c1)) {
          return true;
        }

        final var c2 = c1.withValue(c0.value());
        return c0.equals(c2);
      });
  }

  @Test
  public void testToString()
  {
    QuickTheory.qt()
      .forAll(
        SourceDSL.strings().betweenCodePoints(0x41, 0x5a).ofLengthBetween(
          1,
          128),
        SourceDSL.strings().betweenCodePoints(0x41, 0x5a).ofLengthBetween(
          1,
          128))
      .check((s0, s1) -> {
        if (s0.equals(s1)) {
          return OBName.of(s0).toString().equals(OBName.of(s1).toString());
        } else {
          return !OBName.of(s0).toString().equals(OBName.of(s1).toString());
        }
      });
  }

  @Test
  public void testEquals()
  {
    EqualsVerifier.forClass(OBName.class)
      .withNonnullFields("value")
      .verify();
  }
}
