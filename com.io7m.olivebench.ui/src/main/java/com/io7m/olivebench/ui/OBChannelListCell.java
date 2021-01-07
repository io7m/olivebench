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

import com.io7m.jtensors.core.parameterized.vectors.PVector3D;
import com.io7m.olivebench.controller.OBControllerType;
import com.io7m.olivebench.model.graph.OBChannelMetadata;
import com.io7m.olivebench.model.graph.OBChannelType;
import com.io7m.olivebench.model.spaces.OBSpaceRGBType;
import com.io7m.olivebench.strings.OBStringsType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public final class OBChannelListCell
  extends ListCell<OBChannelType> implements Initializable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBChannelListCell.class);

  @FXML
  private TextField channelNameField;

  @FXML
  private TextField channelIdField;

  @FXML
  private ImageView channelIcon;

  @FXML
  private ColorPicker channelColorPicker;

  @FXML
  private Pane channelCell;
  private OBControllerType controller;

  @FXML
  private void onChannelNameChangeSelected()
  {

  }

  public OBChannelListCell()
  {

  }

  public static OBChannelListCell newInstance(
    final OBControllerType controller,
    final OBStringsType strings)
  {
    final FXMLLoader loader =
      new FXMLLoader(
        OBChannelListCell.class.getResource("channelListCell.fxml"),
        strings.resourceBundle()
      );

    try {
      loader.load();
      final OBChannelListCell cell = loader.getController();
      cell.setController(controller);
      return cell;
    } catch (final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  private void setController(
    final OBControllerType inController)
  {
    this.controller =
      Objects.requireNonNull(inController, "inController");
  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {
    Objects.requireNonNull(location, "location");
    Objects.requireNonNull(resources, "resources");

    this.setGraphic(this.channelCell);
    this.setText(null);
    this.channelColorPicker.getStylesheets().add(OBCSS.stylesheet());
  }

  @Override
  protected void updateItem(
    final OBChannelType item,
    final boolean empty)
  {
    super.updateItem(item, empty);

    if (item == null || empty) {
      this.channelCell.setVisible(false);
      this.channelColorPicker.setOnAction(null);
      return;
    }

    final var nodeMetadata =
      item.nodeMetadata().read();
    final var channelMetadata =
      item.channelMetadata().read();
    final var channelColor =
      channelMetadata.color();

    this.channelCell.setVisible(true);
    this.channelNameField.setText(nodeMetadata.name().value());
    this.channelIdField.setText(item.id().toString());
    this.channelColorPicker.setValue(
      Color.color(channelColor.x(), channelColor.y(), channelColor.z())
    );

    this.channelColorPicker.setOnAction(event -> {
      final var color = this.channelColorPicker.getValue();
      final var colorVector =
        PVector3D.<OBSpaceRGBType>of(
          color.getRed(),
          color.getGreen(),
          color.getBlue()
        );

      this.controller.updateChannelMetadata(
        item.id(),
        existing -> updateChannelMetadata(existing, colorVector)
      );
    });
  }

  private static OBChannelMetadata updateChannelMetadata(
    final OBChannelMetadata channelMetadata,
    final PVector3D<OBSpaceRGBType> colorVector)
  {
    return OBChannelMetadata.builder()
      .from(channelMetadata)
      .setColor(colorVector)
      .build();
  }
}
