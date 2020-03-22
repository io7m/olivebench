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

package com.io7m.olivebench.model;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

@ImmutablesStyleType
@Value.Immutable
public interface OBTimeConfigurationType
{
  long ticksPerQuarterNote();

  default long ticksPerBar()
  {
    return this.ticksPerQuarterNote() * 4L;
  }

  default double millisecondsPerTick(
    final long beatsPerMinute)
  {
    return 60000.0 / (double) (beatsPerMinute * this.ticksPerQuarterNote());
  }

  @Value.Check
  default OBTimeConfigurationType checkPreconditions()
  {
    if (this.ticksPerQuarterNote() < 0L) {
      return OBTimeConfiguration.builder()
        .setTicksPerQuarterNote(1L)
        .build();
    }
    return this;
  }
}
