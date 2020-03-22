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

import com.io7m.olivebench.controller.OBControllerType;
import com.io7m.olivebench.model.metadata.OBMetadataProperty;
import com.io7m.olivebench.model.metadata.OBMetadatas;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public final class OBEditMetadataPropertyDialog
{
  private static final Logger LOG = LoggerFactory.getLogger(
    OBEditMetadataPropertyDialog.class);

  private final OBControllerType controller;

  @FXML
  private TextField propertyField;

  @FXML
  private TextField valueField;

  @FXML
  private Button cancelButton;

  @FXML
  private Button confirmButton;

  private Stage dialog;
  private OBMetadataProperty property;

  private OBEditMetadataPropertyDialog(
    final OBControllerType inController)
  {
    this.controller = inController;
  }

  public static OBEditMetadataPropertyDialog create(
    final OBControllerType controller)
  {
    return new OBEditMetadataPropertyDialog(controller);
  }

  public void show(
    final OBMetadataProperty property)
    throws IOException
  {
    Objects.requireNonNull(property, "property");

    OBFXThread.checkIsUIThread();

    final var editXML =
      OBEditMetadataPropertyDialog.class.getResource("editProperty.fxml");

    final var strings = this.controller.strings();
    final var resources = strings.resourceBundle();
    final var loader = new FXMLLoader(editXML, resources);

    loader.setController(this);
    final Pane pane = loader.load();

    this.property = property;
    this.propertyField.setText(property.name());
    this.valueField.setText(property.value());

    this.dialog = new Stage();
    this.dialog.initModality(Modality.APPLICATION_MODAL);
    this.dialog.setScene(new Scene(pane));
    this.dialog.setTitle(property.name());
    this.dialog.show();
  }

  @FXML
  private void onConfirmSelected()
  {
    LOG.debug("confirm selected");

    final var key = this.property.name();
    final var value = this.valueField.getText();
    this.controller.updateMetadata(m -> OBMetadatas.put(m, key, value));
    this.close();
  }

  @FXML
  private void onCancelSelected()
  {
    LOG.debug("cancel selected");
    this.dialog.close();
  }

  public void close()
  {
    OBFXThread.checkIsUIThread();

    this.dialog.close();
  }
}
