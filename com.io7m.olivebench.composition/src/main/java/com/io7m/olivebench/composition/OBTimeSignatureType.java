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
import org.immutables.value.Value;

/**
 * A time signature specification.
 */

@ImmutablesStyleType
@Value.Immutable(copy = false, builder = false)
public interface OBTimeSignatureType extends OBShowableType
{
  /**
   * The upper numeral of the time signature: The number of beats that constitute
   * a bar.
   *
   * @return The upper numeral of the time signature
   */

  @Value.Parameter
  int upperNumeral();

  /**
   * The lower numeral of the time signature: The note value that constitutes
   * one beat.
   *
   * @return The lower numeral of the time signature
   */

  @Value.Parameter
  int lowerNumeral();

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    this.checkUpperNumeral();
    this.checkLowerNumeral();
  }

  /**
   * @return The note value that constitutes one beat.
   */

  default int beatValue()
  {
    return this.lowerNumeral();
  }

  /**
   * @return The number of beats that constitute one bar
   */

  default int beatsPerBar()
  {
    return this.upperNumeral();
  }

  private void checkUpperNumeral()
  {
    if (this.upperNumeral() == 0) {
      throw this.badUpperNumeral();
    }
  }

  private void checkLowerNumeral()
  {
    if (this.lowerNumeral() == 0) {
      throw this.badLowerNumeral();
    }
    if (this.lowerNumeral() == 1) {
      return;
    }
    if (this.lowerNumeral() == 2) {
      return;
    }
    if (this.lowerNumeral() % 4 == 0) {
      return;
    }
    throw this.badLowerNumeral();
  }

  private IllegalArgumentException badUpperNumeral()
  {
    return new IllegalArgumentException(String.format(
      "Invalid upper numeral: %d",
      Integer.valueOf(this.upperNumeral()))
    );
  }

  private IllegalArgumentException badLowerNumeral()
  {
    return new IllegalArgumentException(String.format(
      "Invalid lower numeral: %d",
      Integer.valueOf(this.lowerNumeral()))
    );
  }

  @Override
  default String show()
  {
    return String.format(
      "%d/%d",
      Integer.valueOf(this.upperNumeral()),
      Integer.valueOf(this.lowerNumeral())
    );
  }

  /**
   * @param ticksPerQuarterNote The ticks per quarter note in the composition
   *
   * @return The number of ticks that constitute one beat
   */

  default long ticksPerBeat(
    final long ticksPerQuarterNote)
  {
    switch (this.beatValue()) {
      case 1: {
        return ticksPerQuarterNote * 4L;
      }
      case 2: {
        return ticksPerQuarterNote * 2L;
      }
      case 4: {
        return ticksPerQuarterNote;
      }
      default: {
        final var factor = 4.0 / (double) this.beatValue();
        return (long) (ticksPerQuarterNote * factor);
      }
    }
  }
}
