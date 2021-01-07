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

package com.io7m.olivebench.ui;

import com.io7m.olivebench.controller.OBControllerEventCompositionStatusChanged;
import com.io7m.olivebench.controller.OBControllerEventCompositionStatusChangedType.Status;
import com.io7m.olivebench.controller.OBControllerType;
import com.io7m.olivebench.model.OBCompositionReadableType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.strings.OBStringsType;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.olivebench.model.graph.OBRegionType.OBDecorativeRegionType.OBTextRegionType;

public final class OBCompositionViewController implements OBViewControllerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBCompositionViewController.class);

  private final CompositeDisposable subscriptions;
  private final SimpleDoubleProperty ticksPerPixel;
  private final SimpleDoubleProperty pixelsPerNote;

  @FXML
  private ScrollPane compositionScrollArea;
  @FXML
  private Pane compositionScrollContent;
  @FXML
  private Pane compositionGridCanvasContainer;
  @FXML
  private Canvas compositionGridCanvas;

  private OBStringsType strings;
  private OBControllerType controller;

  public OBCompositionViewController()
  {
    this.subscriptions = new CompositeDisposable();
    this.ticksPerPixel = new SimpleDoubleProperty(60.0);
    this.pixelsPerNote = new SimpleDoubleProperty(16.0);
  }

  @FXML
  private void onToolCursorSelected()
  {
    LOG.trace("onToolCursorSelected");
  }

  @FXML
  private void onToolDrawSelected()
  {
    LOG.trace("onToolDrawSelected");
  }

  @FXML
  private void onToolZoomSelected()
  {
    LOG.trace("onToolZoomSelected");
  }

  @Override
  public void initialize(
    final OBServiceDirectoryType services)
  {
    this.strings =
      services.requireService(OBStringsType.class);
    this.controller =
      services.requireService(OBControllerType.class);

    this.compositionScrollContent.setScaleY(-1.0);

    this.compositionGridCanvasContainer.widthProperty()
      .addListener((observable, oldValue, newValue) -> {
      this.compositionGridCanvas.setWidth(newValue.doubleValue());
      this.redrawCanvas();
    });
    this.compositionGridCanvasContainer.heightProperty()
      .addListener((observable, oldValue, newValue) -> {
        this.compositionGridCanvas.setHeight(newValue.doubleValue());
        this.redrawCanvas();
      });

    this.compositionScrollArea.viewportBoundsProperty()
      .addListener((observable, oldValue, newValue) -> this.redrawCanvas());
    this.compositionScrollArea.hvalueProperty()
      .addListener((observable, oldValue, newValue) -> this.redrawCanvas());
    this.compositionScrollArea.vvalueProperty()
      .addListener((observable, oldValue, newValue) -> this.redrawCanvas());

    this.subscriptions.add(
      this.controller.events()
        .ofType(OBControllerEventCompositionStatusChanged.class)
        .subscribe(this::onCompositionStatusChanged)
    );

    this.compositionGridCanvas.setWidth(
      this.compositionGridCanvasContainer.getWidth());
    this.compositionGridCanvas.setHeight(
      this.compositionGridCanvasContainer.getHeight());
    this.redrawCanvas();
  }

  private void redrawCanvas()
  {
    final double hmin =
      this.compositionScrollArea.getHmin();
    final double hmax =
      this.compositionScrollArea.getHmax();
    final double hvalue =
      this.compositionScrollArea.getHvalue();
    final double contentWidth =
      this.compositionScrollContent.getLayoutBounds().getWidth();
    final double viewportWidth =
      this.compositionScrollArea.getViewportBounds().getWidth();

    final double xMinimum =
      Math.max(
        0.0,
        contentWidth - viewportWidth) * (hvalue - hmin) / (hmax - hmin);

    final double xMaximum =
      xMinimum + viewportWidth;

    final double vmin =
      this.compositionScrollArea.getVmin();
    final double vmax =
      this.compositionScrollArea.getVmax();
    final double vvalue =
      this.compositionScrollArea.getVvalue();
    final double contentHeight =
      this.compositionScrollContent.getLayoutBounds().getHeight();
    final double viewportHeight =
      this.compositionScrollArea.getViewportBounds().getHeight();

    final double yMinimum =
      Math.max(
        0.0,
        contentHeight - viewportHeight) * (vvalue - vmin) / (vmax - vmin);

    final double yMaximum =
      yMinimum + viewportHeight;

    final var graphics = this.compositionGridCanvas.getGraphicsContext2D();
    graphics.clearRect(
      0.0,
      0.0,
      this.compositionGridCanvas.getWidth(),
      this.compositionGridCanvas.getHeight()
    );

    graphics.fillRect(
      0.0,
      0.0,
      this.compositionGridCanvas.getWidth(),
      this.compositionGridCanvas.getHeight()
    );
  }

  private static double roundUp(
    final double value,
    final double multiple)
  {
    return Math.ceil(value / multiple) * multiple;
  }

  private static double roundDown(
    final double value,
    final double multiple)
  {
    return Math.floor(value / multiple) * multiple;
  }

  private void onCompositionStatusChanged(
    final OBControllerEventCompositionStatusChanged event)
  {
    Platform.runLater(() -> {
      if ((event.statusThen() == Status.STATUS_NOT_LOADED)
        && (event.statusNow() != Status.STATUS_NOT_LOADED)) {
        this.ticksPerPixel.set(60.0);
        this.createInitialRegions(
          this.controller.compositionSnapshot().orElseThrow()
        );
      }
    });
  }

  private void createInitialRegions(
    final OBCompositionReadableType composition)
  {
    composition.graph()
      .nodes()
      .values()
      .stream()
      .filter(n -> n instanceof OBTextRegionType)
      .map(n -> (OBTextRegionType) n)
      .forEach(region -> {
        final var area = region.nodeArea();
        final var textPane = new TextField(region.text());
        textPane.setLayoutX(area.minimumX());
        textPane.setLayoutY(area.minimumY() * this.pixelsPerNote.get());
        textPane.setPrefWidth(area.sizeX());
        textPane.setPrefHeight(area.sizeY() * this.pixelsPerNote.get());
        textPane.setAlignment(Pos.CENTER);
        textPane.setEditable(false);
        textPane.setDisable(true);
        textPane.setScaleY(-1.0);
        this.compositionScrollContent.getChildren().add(textPane);
      });
  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBCompositionViewController 0x%s]",
      Long.toUnsignedString(System.identityHashCode(this)));
  }
}
