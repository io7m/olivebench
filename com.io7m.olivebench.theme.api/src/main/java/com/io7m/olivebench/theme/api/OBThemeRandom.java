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

package com.io7m.olivebench.theme.api;

import com.io7m.jtensors.core.parameterized.vectors.PVector4D;
import com.io7m.olivebench.composition.spaces.OBRGBASpaceType;

import java.util.Map;
import java.util.UUID;

import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_BACKGROUND;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_BORDER;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_NOTE_LINE;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_REGION_BACKGROUND;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_REGION_BORDER;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_REGION_NOTE_BORDER;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_REGION_NOTE_FILL;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_REGION_NOTE_HIGHLIGHT_GREATER;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_REGION_NOTE_HIGHLIGHT_LESSER;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_REGION_NOTE_HIGHLIGHT_ROOT;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_REGION_NOTE_LINE;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_REGION_TEXT;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_REGION_TICK_BAR;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_REGION_TICK_BEAT;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_TEXT;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_TICK_BAR;
import static com.io7m.olivebench.theme.api.OBThemeType.PATTERN_TICK_BEAT;
import static com.io7m.olivebench.theme.api.OBThemeType.TIMELINE_BACKGROUND;
import static com.io7m.olivebench.theme.api.OBThemeType.TIMELINE_TEXT;
import static com.io7m.olivebench.theme.api.OBThemeType.TIMELINE_TICK_BAR;
import static com.io7m.olivebench.theme.api.OBThemeType.TIMELINE_TICK_BEAT;

/**
 * A theme consisting of random colors. Use at your own risk.
 */

public final class OBThemeRandom
{
  private static final OBTheme THEME = makeTheme();

  private static PVector4D<OBRGBASpaceType> random()
  {
    return PVector4D.of(Math.random(), Math.random(), Math.random(), 1.0);
  }

  private static PVector4D<OBRGBASpaceType> randomTranslucent(
    final double alpha)
  {
    return PVector4D.of(Math.random(), Math.random(), Math.random(), alpha);
  }

  private static OBTheme makeTheme()
  {
    final var colors =
      Map.ofEntries(
        Map.entry(TIMELINE_BACKGROUND, random()),
        Map.entry(TIMELINE_TICK_BAR, randomTranslucent(0.4)),
        Map.entry(TIMELINE_TICK_BEAT, randomTranslucent(0.1)),
        Map.entry(TIMELINE_TEXT, random()),

        Map.entry(PATTERN_REGION_BACKGROUND, randomTranslucent(0.25)),
        Map.entry(PATTERN_REGION_BORDER, randomTranslucent(0.2)),
        Map.entry(PATTERN_REGION_TICK_BAR, randomTranslucent(0.4)),
        Map.entry(PATTERN_REGION_TICK_BEAT, randomTranslucent(0.1)),
        Map.entry(PATTERN_REGION_TEXT, random()),
        Map.entry(PATTERN_REGION_NOTE_LINE, randomTranslucent(0.05)),
        Map.entry(
          PATTERN_REGION_NOTE_HIGHLIGHT_ROOT,
          randomTranslucent(0.12)),
        Map.entry(
          PATTERN_REGION_NOTE_HIGHLIGHT_GREATER,
          randomTranslucent(0.08)),
        Map.entry(
          PATTERN_REGION_NOTE_HIGHLIGHT_LESSER,
          randomTranslucent(0.04)),
        Map.entry(PATTERN_REGION_NOTE_BORDER, random()),
        Map.entry(PATTERN_REGION_NOTE_FILL, random()),

        Map.entry(PATTERN_BACKGROUND, random()),
        Map.entry(PATTERN_BORDER, random()),
        Map.entry(PATTERN_TICK_BAR, randomTranslucent(0.4)),
        Map.entry(PATTERN_TICK_BEAT, randomTranslucent(0.1)),
        Map.entry(PATTERN_TEXT, random()),
        Map.entry(PATTERN_NOTE_LINE, randomTranslucent(0.05))
      );

    return OBTheme.builder()
      .setId(UUID.fromString("b31ac8a3-9a6c-4a25-88a6-6b3eba163343"))
      .setName("Random")
      .setColors(colors)
      .build();
  }

  private OBThemeRandom()
  {

  }

  /**
   * @return A reference to the theme
   */

  public static OBTheme get()
  {
    return THEME;
  }
}
