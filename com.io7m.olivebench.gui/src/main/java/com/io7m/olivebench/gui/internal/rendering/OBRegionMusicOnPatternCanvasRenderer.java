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

package com.io7m.olivebench.gui.internal.rendering;

import com.io7m.jregions.core.parameterized.areas.PAreaL;
import com.io7m.jtensors.core.parameterized.vectors.PVector2L;
import com.io7m.jtensors.core.parameterized.vectors.PVector4D;
import com.io7m.jtensors.core.parameterized.vectors.PVectors4D;
import com.io7m.olivebench.composition.OBCompositionType;
import com.io7m.olivebench.composition.OBKeySignature;
import com.io7m.olivebench.composition.regions.OBNote;
import com.io7m.olivebench.composition.regions.OBRegionMusicType;
import com.io7m.olivebench.composition.spaces.OBRGBASpaceType;
import com.io7m.olivebench.composition.spaces.OBTrackSpaceType;
import com.io7m.olivebench.controller.api.OBControllerReadableType;

import java.util.Objects;
import java.util.Optional;

public final class OBRegionMusicOnPatternCanvasRenderer
  implements OBRendererType<OBRegionMusicType>
{
  private final OBControllerReadableType controller;

  public OBRegionMusicOnPatternCanvasRenderer(
    final OBControllerReadableType inController)
  {
    this.controller =
      Objects.requireNonNull(inController, "controller");
  }

  @Override
  public void render(
    final OBRenderContextType context,
    final OBRegionMusicType region)
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(region, "item");

    final var track =
      region.track();
    final var composition =
      track.composition();
    final var screenBoundsOfRegion =
      context.trackToScreen(region.bounds());

    final var theme =
      this.controller.theme();
    final var trackIsActive =
      this.controller.trackIsActive(track);

    final var trackColor =
      track.metadata().colorRGBA();

    if (trackIsActive) {
      final var fill =
        PVectors4D.multiply(
          theme.patternRegionBackground(),
          trackColor
        );

      context.drawRectScreen(
        screenBoundsOfRegion,
        Optional.empty(),
        Optional.of(fill)
      );
      this.renderBarBeatTicks(context, composition, region);
      this.renderNoteLines(context, composition, region);
    } else {
      final var fill =
        PVectors4D.multiply(
          theme.inactiveOf(theme.patternRegionBackground()),
          trackColor
        );

      context.drawRectScreen(
        screenBoundsOfRegion,
        Optional.of(theme.inactiveOf(theme.patternRegionBorder())),
        Optional.of(fill)
      );
    }

    var noteStroke =
      theme.patternRegionNoteBorder();
    final var baseFill =
      theme.patternRegionNoteFill();
    var noteFill =
      PVectors4D.multiply(baseFill, trackColor);

    if (!trackIsActive) {
      noteStroke =
        theme.inactiveOf(noteStroke);
      noteFill =
        theme.inactiveOf(noteFill);
    }

    for (final var note : region.notes()) {
      renderNote(
        context,
        region,
        noteStroke,
        noteFill,
        note
      );
    }
  }

  private void renderNoteLines(
    final OBRenderContextType context,
    final OBCompositionType composition,
    final OBRegionMusicType region)
  {
    final var theme =
      this.controller.theme();
    final var lineColor =
      theme.patternRegionNoteLine();

    final var regionBounds =
      region.bounds();
    final var x0 =
      regionBounds.minimumX();
    final var x1 =
      regionBounds.maximumX();
    final var minNote =
      regionBounds.minimumY();
    final var maxNote =
      regionBounds.maximumY();

    final var keySignature =
      region.keySignature();

    for (var note = minNote; note <= maxNote; ++note) {
      final var p0 =
        PVector2L.<OBTrackSpaceType>of(x0, note);
      final var p1 =
        PVector2L.<OBTrackSpaceType>of(x1, note);
      context.drawLineTrack(lineColor, p0, p1);

      if (keySignature.isRoot(note)) {
        this.renderNoteRowHighlights(
          context,
          composition,
          keySignature,
          x0,
          x1,
          note
        );
      }
    }
  }

  private void renderNoteRowHighlights(
    final OBRenderContextType context,
    final OBCompositionType composition,
    final OBKeySignature keySignature,
    final long x0,
    final long x1,
    final long note)
  {
    final var theme =
      this.controller.theme();

    final var highlightRootColor =
      theme.patternRegionNoteHighlightRoot();
    final var highlightGreaterColor =
      theme.patternRegionNoteHighlightGreater();
    final var highlightLesserColor =
      theme.patternRegionNoteHighlightLesser();

    final var pitchClasses =
      keySignature.significantPitchClassMap();

    final var noteY =
      composition.noteRange().upper() - note;
    final var noteY1 =
      noteY + 1L;

    final var area =
      context.trackToScreen(PAreaL.of(x0, x1, noteY, noteY1));

    context.drawRectScreen(
      area,
      Optional.empty(),
      Optional.of(highlightRootColor)
    );

    final var notesPerOctave = (long) keySignature.notesPerOctave();
    for (var relativePitch = 1L; relativePitch < notesPerOctave; ++relativePitch) {
      final var significant =
        pitchClasses.get(Integer.valueOf((int) relativePitch));

      if (significant != null) {
        final var otherNoteY = noteY + relativePitch;
        final var otherNoteY1 = otherNoteY + 1L;
        final var otherArea =
          context.trackToScreen(PAreaL.of(x0, x1, otherNoteY, otherNoteY1));

        switch (significant.significance()) {
          case GREATER: {
            context.drawRectScreen(
              otherArea,
              Optional.empty(),
              Optional.of(highlightGreaterColor)
            );
            break;
          }
          case LESSER: {
            context.drawRectScreen(
              otherArea,
              Optional.empty(),
              Optional.of(highlightLesserColor)
            );
            break;
          }
        }
      }
    }
  }

  private void renderBarBeatTicks(
    final OBRenderContextType context,
    final OBCompositionType composition,
    final OBRegionMusicType region)
  {
    final var timeConfiguration =
      composition.metadata().timeConfiguration();
    final var ticksPerQuarterNote =
      timeConfiguration.ticksPerQuarterNote();

    final var timeSignature =
      region.timeSignature();
    final var ticksPerBeat =
      timeSignature.ticksPerBeat(ticksPerQuarterNote);
    final var beatsPerBar =
      timeSignature.beatsPerBar();

    final var theme =
      this.controller.theme();
    final var colorTickBeat =
      theme.patternRegionTickBeat();
    final var colorTickBar =
      theme.patternRegionTickBar();

    final var compositionBounds =
      composition.noteRange();
    final var minY =
      compositionBounds.lower();
    final var maxY =
      compositionBounds.upper() + 1L;

    final var regionBounds =
      region.bounds();

    var beats = 0L;
    for (var x = regionBounds.minimumX(); x <= regionBounds.maximumX(); x += ticksPerBeat) {
      final var p0 =
        PVector2L.<OBTrackSpaceType>of(x, minY);
      final var p1 =
        PVector2L.<OBTrackSpaceType>of(x, maxY);

      context.drawLineTrack(colorTickBeat, p0, p1);
      if (beats % (long) beatsPerBar == 0L) {
        context.drawLineTrack(colorTickBar, p0, p1);
      }
      ++beats;
    }
  }

  private static void renderNote(
    final OBRenderContextType context,
    final OBRegionMusicType region,
    final PVector4D<OBRGBASpaceType> noteStroke,
    final PVector4D<OBRGBASpaceType> noteFill,
    final OBNote note)
  {
    final var noteRange =
      region.track().composition().noteRange();

    final var noteY =
      noteRange.upper() - note.note();
    final var noteY1 =
      noteY + 1L;

    final var noteLocalXMin = note.start();
    final var noteLocalXMax = note.start() + note.length();
    final var noteTrackXMin = region.bounds().minimumX() + noteLocalXMin;
    final var noteTrackXMax = region.bounds().minimumX() + noteLocalXMax;

    final var area =
      context.trackToScreen(
        PAreaL.of(
          noteTrackXMin,
          noteTrackXMax,
          noteY,
          noteY1
        )
      );

    context.drawRectRoundedScreen(
      area,
      Optional.of(noteStroke),
      Optional.of(noteFill)
    );
  }
}
