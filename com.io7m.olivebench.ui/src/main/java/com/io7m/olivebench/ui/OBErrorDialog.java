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

import com.io7m.olivebench.controller.OBControllerEventTaskFailed;
import com.io7m.olivebench.controller.OBControllerType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class OBErrorDialog
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBErrorDialog.class);

  private final OBControllerType controller;

  @FXML
  private TextArea textArea;

  @FXML
  private Button okButton;

  @FXML
  private Button errorButton;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorMessage;

  private Stage dialog;

  private OBErrorDialog(
    final OBControllerType inController)
  {
    this.controller = inController;
  }

  public static OBErrorDialog create(
    final OBControllerType controller)
  {
    return new OBErrorDialog(controller);
  }

  // CHECKSTYLE:OFF
  private static String stringOf(final byte[] byteArray)
  {
    return new String(byteArray, UTF_8);
    // CHECKSTYLE:ON
  }

  public Stage dialog()
  {
    return this.dialog;
  }

  @FXML
  private void onOKSelected()
  {
    LOG.debug("closing error dialog (OK)");
    this.dialog.close();
  }

  @FXML
  private void onSendErrorSelected()
  {
    LOG.debug("closing error dialog (Send error)");
    this.dialog.close();
  }

  public void show(
    final OBControllerEventTaskFailed error)
    throws IOException
  {
    OBFXThread.checkIsUIThread();

    final var errorXML = OBErrorDialog.class.getResource("error.fxml");
    Objects.requireNonNull(errorXML, "errorXML");

    final var resources = this.controller.strings().resourceBundle();
    final var loader = new FXMLLoader(errorXML, resources);

    loader.setController(this);
    final AnchorPane pane = loader.load();

    this.dialog = new Stage();
    this.dialog.initModality(Modality.APPLICATION_MODAL);
    this.dialog.setScene(new Scene(pane));
    this.dialog.setTitle(error.title());

    this.errorMessage.setText(error.title());
    this.textArea.appendText(error.message());
    this.textArea.appendText("\n");
    this.textArea.appendText("\n");

    final var exceptionOpt = error.exception();
    if (exceptionOpt.isPresent()) {
      final var exception = exceptionOpt.get();
      final var bytes = new ByteArrayOutputStream();
      try (var writer = new PrintStream(bytes, false, UTF_8)) {
        exception.printStackTrace(writer);
        writer.flush();
      }

      this.textArea.appendText(stringOf(bytes.toByteArray()));
    }

    this.textArea.positionCaret(0);
    this.dialog.show();
  }
}
