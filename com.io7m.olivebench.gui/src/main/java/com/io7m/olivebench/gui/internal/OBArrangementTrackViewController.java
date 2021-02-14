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

import com.io7m.olivebench.composition.OBTrackType;
import com.io7m.olivebench.controller.api.OBControllerAsynchronousType;
import com.io7m.olivebench.gui.internal.rendering.OBColors;
import com.io7m.olivebench.gui.internal.rendering.OBRenderContext;
import com.io7m.olivebench.gui.internal.rendering.OBTrackOnArrangementCanvasRenderer;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public final class OBArrangementTrackViewController
  extends ListCell<OBTrackType> implements Initializable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBArrangementTrackViewController.class);

  private final OBServiceDirectoryType services;
  private final OBControllerAsynchronousType controller;
  private final CompositeDisposable compositionSubscriptions;
  private final OBTrackOnArrangementCanvasRenderer trackRenderer;

  @FXML
  private AnchorPane rootPane;
  @FXML
  private TextField trackNameField;
  @FXML
  private ToggleButton muteButton;
  @FXML
  private ToggleButton soloButton;
  @FXML
  private Rectangle shade;

  private OBCanvasPane trackCanvas;
  private OBTrackType track;
  private OBRenderContext trackRenderContext;

  private OBArrangementTrackViewController(
    final OBServiceDirectoryType inServices)
  {
    this.services =
      Objects.requireNonNull(inServices, "services");
    this.controller =
      this.services.requireService(OBControllerAsynchronousType.class);
    this.compositionSubscriptions =
      new CompositeDisposable();
    this.trackRenderer =
      new OBTrackOnArrangementCanvasRenderer(this.controller);
  }

  public static OBArrangementTrackViewController create(
    final OBServiceDirectoryType services)
  {
    final var strings =
      services.requireService(OBMainStrings.class);

    final FXMLLoader loader =
      new FXMLLoader(
        OBArrangementTrackViewController.class.getResource(
          "arrangementTrack.fxml"),
        strings.resources()
      );

    loader.setControllerFactory(aClass -> {
      if (Objects.equals(aClass, OBArrangementTrackViewController.class)) {
        return new OBArrangementTrackViewController(services);
      }
      throw new IllegalStateException(
        String.format("Unrecognized class: %s", aClass)
      );
    });

    try {
      loader.load();
      return loader.getController();
    } catch (final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  @Override
  public void initialize(
    final URL url,
    final ResourceBundle resourceBundle)
  {
    LOG.debug("initialize");

    this.setStyle("-fx-padding: 0px");
    this.setGraphic(this.rootPane);
    this.setText(null);

    final var shadeColor =
      this.controller.theme()
        .arrangementTrackShade();

    this.shade.setFill(OBColors.paintOf(shadeColor));

    this.trackCanvas =
      new OBCanvasPane(this::renderCanvas);

    this.rootPane.getChildren()
      .add(0, this.trackCanvas);

    AnchorPane.setTopAnchor(this.trackCanvas, Double.valueOf(0.0));
    AnchorPane.setBottomAnchor(this.trackCanvas, Double.valueOf(0.0));
    AnchorPane.setLeftAnchor(this.trackCanvas, Double.valueOf(0.0));
    AnchorPane.setRightAnchor(this.trackCanvas, Double.valueOf(0.0));

    this.trackRenderContext =
      new OBRenderContext(this.controller, this.trackCanvas.canvas());
  }

  private void renderCanvas()
  {
    final var currentTrack = this.track;
    if (currentTrack == null) {
      return;
    }

    final var graphics =
      this.trackCanvas.canvas().getGraphicsContext2D();
    graphics.clearRect(
      0.0,
      0.0,
      this.trackCanvas.getWidth(),
      this.trackCanvas.getHeight()
    );

    this.trackRenderer.render(this.trackRenderContext, currentTrack);
  }

  @Override
  protected void updateItem(
    final OBTrackType item,
    final boolean empty)
  {
    super.updateItem(item, empty);

    if (item == null || empty) {
      this.rootPane.setVisible(false);
      this.track = null;
      return;
    }

    this.track = item;
    final var trackMetadata = item.metadata();
    this.rootPane.setVisible(true);
    this.trackNameField.setText(trackMetadata.name());
    this.trackCanvas.invalidate();
  }

  @FXML
  private void onRequestMute()
  {

  }

  @FXML
  private void onRequestSolo()
  {

  }

  @FXML
  private void onTextEdited()
  {
    this.controller.trackSetMetadata(
      this.track,
      this.track.metadata().withName(this.trackNameField.getText())
    );
  }
}
