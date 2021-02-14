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

package com.io7m.olivebench.gui.internal;

import com.io7m.olivebench.composition.OBCompositionEventType;
import com.io7m.olivebench.composition.OBCompositionModificationTimeChangedEvent;
import com.io7m.olivebench.composition.OBCompositionModifiedEvent;
import com.io7m.olivebench.controller.api.OBControllerAsynchronousType;
import com.io7m.olivebench.controller.api.OBControllerCommandEvent;
import com.io7m.olivebench.controller.api.OBControllerCommandFailedEvent;
import com.io7m.olivebench.controller.api.OBControllerCompositionEvent;
import com.io7m.olivebench.controller.api.OBControllerEventType;
import com.io7m.olivebench.gui.internal.rendering.OBRenderContext;
import com.io7m.olivebench.gui.internal.rendering.OBTrackOnPatternCanvasRenderer;
import com.io7m.olivebench.gui.internal.rendering.OBTrackOnPatternTimelineRenderer;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public final class OBPatternViewController implements Initializable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBPatternViewController.class);

  private final OBServiceDirectoryType services;
  private final OBControllerAsynchronousType controller;
  private final CompositeDisposable compositionSubscriptions;
  private final OBTrackOnPatternCanvasRenderer patternTrackRenderer;
  private final OBTrackOnPatternTimelineRenderer timelineTrackRenderer;

  @FXML
  private AnchorPane patternRootPane;
  @FXML
  private Pane toolbar;

  private OBCanvasPane timelineCanvas;
  private OBCanvasPane patternCanvas;
  private OBRenderContext timelineRenderContext;
  private OBRenderContext patternRenderContext;
  private int canvasUpdates;

  public OBPatternViewController(
    final OBServiceDirectoryType inServices)
  {
    this.services =
      Objects.requireNonNull(inServices, "services");
    this.controller =
      this.services.requireService(OBControllerAsynchronousType.class);
    this.compositionSubscriptions =
      new CompositeDisposable();
    this.patternTrackRenderer =
      new OBTrackOnPatternCanvasRenderer(this.controller);
    this.timelineTrackRenderer =
      new OBTrackOnPatternTimelineRenderer(this.controller);
  }

  @Override
  public void initialize(
    final URL url,
    final ResourceBundle resourceBundle)
  {
    LOG.debug("initialize");

    this.timelineCanvas =
      new OBCanvasPane(this::renderCanvasTimeline);
    this.patternCanvas =
      new OBCanvasPane(this::renderCanvasPattern);

    this.timelineCanvas.setMinHeight(32.0);
    this.timelineCanvas.setMaxHeight(32.0);
    this.timelineCanvas.setPrefHeight(32.0);

    this.patternRootPane.getChildren()
      .add(this.timelineCanvas);
    this.patternRootPane.getChildren()
      .add(this.patternCanvas);

    AnchorPane.setTopAnchor(this.timelineCanvas, Double.valueOf(0.0));
    AnchorPane.setLeftAnchor(this.timelineCanvas, Double.valueOf(32.0));
    AnchorPane.setRightAnchor(this.timelineCanvas, Double.valueOf(0.0));

    AnchorPane.setLeftAnchor(this.patternCanvas, Double.valueOf(32.0));
    AnchorPane.setRightAnchor(this.patternCanvas, Double.valueOf(0.0));
    AnchorPane.setTopAnchor(this.patternCanvas, Double.valueOf(32.0));
    AnchorPane.setBottomAnchor(this.patternCanvas, Double.valueOf(0.0));

    this.timelineRenderContext =
      new OBRenderContext(this.controller, this.timelineCanvas.canvas());
    this.patternRenderContext =
      new OBRenderContext(this.controller, this.patternCanvas.canvas());

    this.controller.events()
      .subscribe(this::onControllerEvent);
  }

  private void onControllerEvent(
    final OBControllerEventType event)
  {
    if (event instanceof OBControllerCommandFailedEvent) {
      this.onControllerEventCommandFailed(
        (OBControllerCommandFailedEvent) event);
    } else if (event instanceof OBControllerCommandEvent) {
      this.onControllerEventCommand(
        (OBControllerCommandEvent) event);
    } else if (event instanceof OBControllerCompositionEvent) {
      this.onControllerCompositionEvent(
        (OBControllerCompositionEvent) event);
    } else {
      throw new IllegalStateException();
    }
  }

  private void onControllerCompositionEvent(
    final OBControllerCompositionEvent event)
  {
    Platform.runLater(() -> {
      switch (event.kind()) {
        case COMPOSITION_OPENED: {
          final var composition =
            this.controller.composition()
              .orElseThrow(IllegalStateException::new);

          this.compositionSubscriptions.add(
            composition.events().subscribe(this::onCompositionEvent)
          );
          break;
        }

        case COMPOSITION_VIEWPORT_CHANGED: {
          this.timelineCanvas.invalidate();
          this.patternCanvas.invalidate();
          break;
        }

        case COMPOSITION_UNDO_CHANGED:
        case COMPOSITION_SAVED: {
          break;
        }

        case COMPOSITION_CLOSED: {
          this.compositionSubscriptions.dispose();
          break;
        }
      }
    });
  }

  private void onCompositionEvent(
    final OBCompositionEventType event)
  {
    if (event instanceof OBCompositionModifiedEvent) {
      this.onCompositionModifiedEvent(
        (OBCompositionModifiedEvent) event);
    } else if (event instanceof OBCompositionModificationTimeChangedEvent) {
      this.onCompositionModificationTimeChangedEvent(
        (OBCompositionModificationTimeChangedEvent) event);
    } else {
      throw new IllegalStateException();
    }
  }

  private void onCompositionModificationTimeChangedEvent(
    final OBCompositionModificationTimeChangedEvent event)
  {

  }

  private void onCompositionModifiedEvent(
    final OBCompositionModifiedEvent event)
  {
    Platform.runLater(() -> {
      this.timelineCanvas.invalidate();
      this.patternCanvas.invalidate();
    });
  }

  private void onControllerEventCommand(
    final OBControllerCommandEvent event)
  {

  }

  private void onControllerEventCommandFailed(
    final OBControllerCommandFailedEvent event)
  {

  }

  private void renderCanvasPattern()
  {
    final var compositionOpt = this.controller.composition();
    if (compositionOpt.isEmpty()) {
      return;
    }

    final var composition =
      compositionOpt.get();
    final var graphics =
      this.patternCanvas.canvas().getGraphicsContext2D();

    graphics.clearRect(
      0.0,
      0.0,
      this.patternCanvas.getWidth(),
      this.patternCanvas.getHeight()
    );

    final var tracks =
      List.copyOf(composition.tracks().values());

    for (final var track : tracks) {
      this.patternTrackRenderer.render(this.patternRenderContext, track);
    }
  }

  private void renderCanvasTimeline()
  {
    final var compositionOpt = this.controller.composition();
    if (compositionOpt.isEmpty()) {
      return;
    }

    final var composition =
      compositionOpt.get();
    final var graphics =
      this.timelineCanvas.canvas().getGraphicsContext2D();

    graphics.clearRect(
      0.0,
      0.0,
      this.timelineCanvas.getWidth(),
      this.timelineCanvas.getHeight()
    );

    final var tracks =
      List.copyOf(composition.tracks().values());

    for (final var track : tracks) {
      this.timelineTrackRenderer.render(this.timelineRenderContext, track);
    }
  }
}
