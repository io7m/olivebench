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

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jtensors.core.parameterized.vectors.PVector4D;
import com.io7m.olivebench.composition.spaces.OBRGBASpaceType;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.STATIC;

/**
 * A loaded theme.
 */

@ImmutablesStyleType
@Value.Immutable
public interface OBThemeType
{
  /**
   * The PATTERN_REGION_BACKGROUND property name.
   */

  String PATTERN_REGION_BACKGROUND =
    "pattern.region.background";

  /**
   * The PATTERN_REGION_BORDER property name.
   */

  String PATTERN_REGION_BORDER =
    "pattern.region.border";

  /**
   * The PATTERN_REGION_TICK_BAR property name.
   */

  String PATTERN_REGION_TICK_BAR =
    "pattern.region.tickBar";

  /**
   * The PATTERN_REGION_TICK_BEAT property name.
   */

  String PATTERN_REGION_TICK_BEAT =
    "pattern.region.tickBeat";

  /**
   * The PATTERN_REGION_TEXT property name.
   */

  String PATTERN_REGION_TEXT =
    "pattern.region.text";

  /**
   * The PATTERN_REGION_NOTE_LINE property name.
   */

  String PATTERN_REGION_NOTE_LINE =
    "pattern.region.noteLine";

  /**
   * The PATTERN_REGION_NOTE_HIGHLIGHT_ROOT property name.
   */

  String PATTERN_REGION_NOTE_HIGHLIGHT_ROOT =
    "pattern.region.noteHighlightRoot";

  /**
   * The PATTERN_REGION_NOTE_HIGHLIGHT_GREATER property name.
   */

  String PATTERN_REGION_NOTE_HIGHLIGHT_GREATER =
    "pattern.region.noteHighlightGreater";

  /**
   * The PATTERN_REGION_NOTE_HIGHLIGHT_LESSER property name.
   */

  String PATTERN_REGION_NOTE_HIGHLIGHT_LESSER =
    "pattern.region.noteHighlightLesser";

  /**
   * The PATTERN_REGION_NOTE_BORDER property name.
   */

  String PATTERN_REGION_NOTE_BORDER =
    "pattern.region.noteBorder";

  /**
   * The PATTERN_REGION_NOTE_FILL property name.
   */

  String PATTERN_REGION_NOTE_FILL =
    "pattern.region.noteFill";

  /**
   * The TIMELINE_BACKGROUND property name.
   */

  String TIMELINE_BACKGROUND =
    "timeline.background";

  /**
   * The TIMELINE_TICK_BAR property name.
   */

  String TIMELINE_TICK_BAR =
    "timeline.tickBar";

  /**
   * The TIMELINE_TICK_BEAT property name.
   */

  String TIMELINE_TICK_BEAT =
    "timeline.tickBeat";

  /**
   * The TIMELINE_TEXT property name.
   */

  String TIMELINE_TEXT =
    "timeline.text";

  /**
   * The PATTERN_BACKGROUND property name.
   */

  String PATTERN_BACKGROUND =
    "pattern.background";

  /**
   * The PATTERN_BORDER property name.
   */

  String PATTERN_BORDER =
    "pattern.border";

  /**
   * The PATTERN_TICK_BAR property name.
   */

  String PATTERN_TICK_BAR =
    "pattern.tickBar";

  /**
   * The PATTERN_TICK_BEAT property name.
   */

  String PATTERN_TICK_BEAT =
    "pattern.tickBeat";

  /**
   * The PATTERN_TEXT property name.
   */

  String PATTERN_TEXT =
    "pattern.text";

  /**
   * The PATTERN_NOTE_LINE property name.
   */

  String PATTERN_NOTE_LINE =
    "pattern.noteLine";

  /**
   * The list of defined color property names.
   */

  List<String> THEME_PROPERTY_NAMES =
    Stream.of(OBThemeType.class.getFields())
      .filter(f -> (f.getModifiers() & STATIC) == STATIC)
      .filter(f -> !"THEME_PROPERTY_NAMES".equals(f.getName()))
      .map(f -> {
        try {
          return (String) f.get(OBThemeType.class);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      })
      .sorted()
      .collect(Collectors.toUnmodifiableList());

  /**
   * @return The theme ID
   */

  UUID id();

  /**
   * @return The theme name
   */

  String name();

  /**
   * @return The color definitions in the theme
   */

  Map<String, PVector4D<OBRGBASpaceType>> colors();

  default PVector4D<OBRGBASpaceType> color(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    final var colors = this.colors();
    final var color = colors.get(name);
    return Optional.ofNullable(color)
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("Color is not defined in the theme: %s", name)
      ));
  }

  default PVector4D<OBRGBASpaceType> patternRegionBorder()
  {
    return this.color(PATTERN_REGION_BORDER);
  }

  default PVector4D<OBRGBASpaceType> patternRegionBackground()
  {
    return this.color(PATTERN_REGION_BACKGROUND);
  }

  default PVector4D<OBRGBASpaceType> patternRegionTickBar()
  {
    return this.color(PATTERN_REGION_TICK_BAR);
  }

  default PVector4D<OBRGBASpaceType> patternRegionTickBeat()
  {
    return this.color(PATTERN_REGION_TICK_BEAT);
  }

  default PVector4D<OBRGBASpaceType> patternRegionText()
  {
    return this.color(PATTERN_REGION_TEXT);
  }

  default PVector4D<OBRGBASpaceType> patternRegionNoteLine()
  {
    return this.color(PATTERN_REGION_NOTE_LINE);
  }

  default PVector4D<OBRGBASpaceType> patternRegionNoteHighlightRoot()
  {
    return this.color(PATTERN_REGION_NOTE_HIGHLIGHT_ROOT);
  }

  default PVector4D<OBRGBASpaceType> patternRegionNoteHighlightGreater()
  {
    return this.color(PATTERN_REGION_NOTE_HIGHLIGHT_GREATER);
  }

  default PVector4D<OBRGBASpaceType> patternRegionNoteHighlightLesser()
  {
    return this.color(PATTERN_REGION_NOTE_HIGHLIGHT_LESSER);
  }

  default PVector4D<OBRGBASpaceType> patternRegionNoteBorder()
  {
    return this.color(PATTERN_REGION_NOTE_BORDER);
  }

  default PVector4D<OBRGBASpaceType> patternRegionNoteFill()
  {
    return this.color(PATTERN_REGION_NOTE_FILL);
  }

  default PVector4D<OBRGBASpaceType> timelineBackground()
  {
    return this.color(TIMELINE_BACKGROUND);
  }

  default PVector4D<OBRGBASpaceType> timelineTickBar()
  {
    return this.color(TIMELINE_TICK_BAR);
  }

  default PVector4D<OBRGBASpaceType> timelineTickBeat()
  {
    return this.color(TIMELINE_TICK_BEAT);
  }

  default PVector4D<OBRGBASpaceType> timelineText()
  {
    return this.color(TIMELINE_TEXT);
  }

  default PVector4D<OBRGBASpaceType> patternBorder()
  {
    return this.color(PATTERN_BORDER);
  }

  default PVector4D<OBRGBASpaceType> patternBackground()
  {
    return this.color(PATTERN_BACKGROUND);
  }

  default PVector4D<OBRGBASpaceType> patternTickBar()
  {
    return this.color(PATTERN_TICK_BAR);
  }

  default PVector4D<OBRGBASpaceType> patternTickBeat()
  {
    return this.color(PATTERN_TICK_BEAT);
  }

  default PVector4D<OBRGBASpaceType> patternText()
  {
    return this.color(PATTERN_TEXT);
  }

  default PVector4D<OBRGBASpaceType> patternNoteLine()
  {
    return this.color(PATTERN_NOTE_LINE);
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    for (final var name : THEME_PROPERTY_NAMES) {
      this.color(name);
    }
  }

  default PVector4D<OBRGBASpaceType> inactiveOf(
    final PVector4D<OBRGBASpaceType> color)
  {
    return PVector4D.of(
      color.x(),
      color.y(),
      color.z(),
      color.w() * 0.25
    );
  }
}
