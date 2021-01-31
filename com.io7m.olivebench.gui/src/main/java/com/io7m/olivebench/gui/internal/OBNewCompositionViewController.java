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

import com.io7m.olivebench.composition.OBClockServiceType;
import com.io7m.olivebench.composition.OBDublinCoreMetadata;
import com.io7m.olivebench.composition.OBTimeConfiguration;
import com.io7m.olivebench.controller.api.OBControllerAsynchronousType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class OBNewCompositionViewController implements Initializable
{
  private final OBControllerAsynchronousType controller;
  private final OBLicenseStrings licenseStrings;
  private final OBClockServiceType clock;
  private Stage stage;

  @FXML
  private TextField titleField;
  @FXML
  private TextField creatorField;
  @FXML
  private ComboBox<String> licenseField;
  @FXML
  private TextField identifierField;
  @FXML
  private ComboBox<Integer> timeResolutionField;

  @FXML
  private void requestCreateComposition()
  {
    final var dublinCoreMetadata =
      OBDublinCoreMetadata.builder()
        .setTitle(this.titleField.getText())
        .setCreator(this.creatorField.getText())
        .setDate(this.clock.now().toString())
        .build();

    final var timeConfiguration =
      OBTimeConfiguration.builder()
        .setTicksPerQuarterNote(this.timeResolutionField.getValue().longValue())
        .build();

    final var id =
      UUID.fromString(this.identifierField.getText());

    this.controller.compositionNew(id, timeConfiguration, dublinCoreMetadata);
    this.stage.close();
  }

  @FXML
  private void requestCancel()
  {
    this.stage.close();
  }

  @FXML
  private void requestChooseLicense()
  {

  }

  @FXML
  private void requestGenerateIdentifier()
  {
    this.identifierField.setText(UUID.randomUUID().toString());
  }

  private OBNewCompositionViewController(
    final OBServiceDirectoryType services)
  {
    Objects.requireNonNull(services, "services");

    this.controller =
      services.requireService(OBControllerAsynchronousType.class);
    this.licenseStrings =
      services.requireService(OBLicenseStrings.class);
    this.clock =
      services.requireService(OBClockServiceType.class);
  }

  public static OBNewCompositionViewController create(
    final OBServiceDirectoryType services)
  {
    Objects.requireNonNull(services, "services");

    final var dialogXML =
      OBNewCompositionViewController.class.getResource("newComposition.fxml");
    Objects.requireNonNull(dialogXML, "dialogXML");

    final var strings =
      services.requireService(OBMainStrings.class);
    final var loader =
      new FXMLLoader(dialogXML, strings.resources());

    loader.setControllerFactory(
      ignored -> new OBNewCompositionViewController(services));

    final AnchorPane pane;
    try {
      pane = loader.load();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    final var dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setScene(new Scene(pane));
    dialog.setTitle(strings.format("compositionCreate"));
    dialog.setMinWidth(480.0);
    dialog.setMinHeight(384.0);

    final var controller = (OBNewCompositionViewController) loader.getController();
    controller.setStage(dialog);

    dialog.show();
    return controller;
  }

  private void setStage(
    final Stage dialog)
  {
    this.stage = Objects.requireNonNull(dialog, "dialog");
  }

  @Override
  public void initialize(
    final URL url,
    final ResourceBundle resourceBundle)
  {
    this.identifierField.setText(UUID.randomUUID().toString());

    this.timeResolutionField.setItems(FXCollections.observableList(
      IntStream.range(4, 12)
        .map(x -> (int) StrictMath.pow(2.0, (double) x))
        .boxed()
        .collect(Collectors.toList())
    ));
    this.timeResolutionField.getSelectionModel().selectLast();

    this.licenseField.setItems(
      FXCollections.observableList(
        this.licenseStrings.resources()
          .keySet()
          .stream()
          .map(this.licenseStrings::format)
          .sorted()
          .collect(Collectors.toList())
      ));

    this.licenseField.getSelectionModel().selectLast();
  }
}
