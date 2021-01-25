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

import com.io7m.olivebench.composition.OBCompositionMetadata;
import com.io7m.olivebench.composition.OBDublinCoreMetadata;
import com.io7m.olivebench.composition.OBDurationD;
import com.io7m.olivebench.composition.OBDurationL;
import com.io7m.olivebench.composition.OBKeySignature;
import com.io7m.olivebench.composition.OBNoteIntervalD;
import com.io7m.olivebench.composition.OBNoteIntervalL;
import com.io7m.olivebench.composition.OBSignificantPitchClass;
import com.io7m.olivebench.composition.OBTimeConfiguration;
import com.io7m.olivebench.composition.OBTimeSignature;
import com.io7m.olivebench.composition.OBTrackMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

public final class OBTestEquality
{
  @TestFactory
  public Stream<DynamicTest> testEquality()
  {
    return Stream.of(
      OBCompositionMetadata.class,
      OBDublinCoreMetadata.class,
      OBDurationD.class,
      OBDurationL.class,
      OBKeySignature.class,
      OBNoteIntervalD.class,
      OBNoteIntervalL.class,
      OBSignificantPitchClass.class,
      OBTimeConfiguration.class,
      OBTimeSignature.class,
      OBTrackMetadata.class
    ).map(this::testOf);
  }

  private DynamicTest testOf(
    final Class<?> clazz)
  {
    return DynamicTest.dynamicTest(
      String.format("testEquals_%s", clazz.getCanonicalName()),
      () -> {
        EqualsVerifier.forClass(clazz)
          .suppress(Warning.NULL_FIELDS)
          .verify();
      }
    );
  }
}
