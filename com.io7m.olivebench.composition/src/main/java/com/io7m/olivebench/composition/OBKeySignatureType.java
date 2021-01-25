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

package com.io7m.olivebench.composition;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jranges.RangeCheck;
import com.io7m.jranges.RangeInclusiveI;
import com.io7m.olivebench.composition.annotations.OBNoteIndexType;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * A key signature.
 */

@ImmutablesStyleType
@Value.Immutable
public interface OBKeySignatureType extends OBShowableType
{
  /**
   * The valid range of notes per octave.
   */

  RangeInclusiveI VALID_NOTES_PER_OCTAVE =
    RangeInclusiveI.of(2, Integer.MAX_VALUE);

  /**
   * @return The number of notes per octave (typically 12)
   */

  int notesPerOctave();

  /**
   * @return The root pitch class offset (0 for C)
   */

  int pitchClassOffset();

  /**
   * @return The name of the key signature
   */

  String name();

  /**
   * @return The significance of various pitch classes in the key
   */

  List<OBSignificantPitchClass> significantPitchClasses();

  /**
   * @return The pitch class significances as a map
   */

  @Value.Auxiliary
  @Value.Derived
  default Map<Integer, OBSignificantPitchClass> significantPitchClassMap()
  {
    return this.significantPitchClasses()
      .stream()
      .collect(toMap(p -> Integer.valueOf(p.pitchClass()), identity()));
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    RangeCheck.checkIncludedInInteger(
      this.notesPerOctave(),
      "Notes per octave",
      VALID_NOTES_PER_OCTAVE,
      "Valid notes per octave"
    );

    final var validPitchClassOffets =
      RangeInclusiveI.of(
        0,
        this.notesPerOctave() - 1
      );

    RangeCheck.checkIncludedInInteger(
      this.pitchClassOffset(),
      "Pitch class offset",
      validPitchClassOffets,
      "Valid pitch class offsets"
    );

    for (final var sig : this.significantPitchClasses()) {
      RangeCheck.checkIncludedInInteger(
        sig.pitchClass(),
        "Significant pitch class",
        validPitchClassOffets,
        "Valid pitch class offsets"
      );
    }
  }

  /**
   * Determine if a given note index is of the pitch class that is the root
   * for this key.
   *
   * @param noteIndex The note index
   *
   * @return {@code true} if noteIndex is the root of this key
   */

  default boolean isRoot(
    final @OBNoteIndexType long noteIndex)
  {
    return (noteIndex % (long) this.notesPerOctave()) == (long) this.pitchClassOffset();
  }

  @Override
  default String show()
  {
    return this.name();
  }
}
