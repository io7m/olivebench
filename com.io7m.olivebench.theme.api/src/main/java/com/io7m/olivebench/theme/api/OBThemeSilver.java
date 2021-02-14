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
 * The default silver theme.
 */

public final class OBThemeSilver
{
  private static final OBTheme THEME = makeTheme();

  private static PVector4D<OBRGBASpaceType> grey(
    final double bright)
  {
    return greyTranslucent(bright, 1.0);
  }

  private static PVector4D<OBRGBASpaceType> greyTranslucent(
    final double bright,
    final double alpha)
  {
    return PVector4D.of(bright, bright, bright, alpha);
  }

  private static OBTheme makeTheme()
  {
    final var colors =
      Map.ofEntries(
        Map.entry(TIMELINE_BACKGROUND, grey(0.8)),
        Map.entry(TIMELINE_TICK_BAR, greyTranslucent(0.0, 0.4)),
        Map.entry(TIMELINE_TICK_BEAT, greyTranslucent(0.0, 0.1)),
        Map.entry(TIMELINE_TEXT, grey(0.1)),

        Map.entry(PATTERN_REGION_BACKGROUND, greyTranslucent(1.0, 0.25)),
        Map.entry(PATTERN_REGION_BORDER, greyTranslucent(0.0, 0.2)),
        Map.entry(PATTERN_REGION_TICK_BAR, greyTranslucent(0.0, 0.4)),
        Map.entry(PATTERN_REGION_TICK_BEAT, greyTranslucent(0.0, 0.1)),
        Map.entry(PATTERN_REGION_TEXT, grey(0.1)),
        Map.entry(PATTERN_REGION_NOTE_LINE, greyTranslucent(0.0, 0.05)),
        Map.entry(
          PATTERN_REGION_NOTE_HIGHLIGHT_ROOT,
          greyTranslucent(0.0, 0.12)),
        Map.entry(
          PATTERN_REGION_NOTE_HIGHLIGHT_GREATER,
          greyTranslucent(0.0, 0.08)),
        Map.entry(
          PATTERN_REGION_NOTE_HIGHLIGHT_LESSER,
          greyTranslucent(0.0, 0.04)),
        Map.entry(PATTERN_REGION_NOTE_BORDER, grey(0.3)),
        Map.entry(PATTERN_REGION_NOTE_FILL, grey(0.8)),

        Map.entry(PATTERN_BACKGROUND, grey(0.8)),
        Map.entry(PATTERN_BORDER, grey(0.1)),
        Map.entry(PATTERN_TICK_BAR, greyTranslucent(0.0, 0.4)),
        Map.entry(PATTERN_TICK_BEAT, greyTranslucent(0.0, 0.1)),
        Map.entry(PATTERN_TEXT, grey(0.1)),
        Map.entry(PATTERN_NOTE_LINE, greyTranslucent(0.0, 0.05))
      );

    return OBTheme.builder()
      .setId(UUID.fromString("319d09cc-e59c-4b95-b1a5-a02b9396403e"))
      .setName("Silver")
      .setColors(colors)
      .build();
  }

  private OBThemeSilver()
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
