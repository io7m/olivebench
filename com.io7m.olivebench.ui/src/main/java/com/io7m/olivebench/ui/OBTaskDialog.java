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

import com.io7m.olivebench.controller.OBControllerEventTaskProgressChanged;
import com.io7m.olivebench.controller.OBControllerEventTaskStarted;
import com.io7m.olivebench.controller.OBControllerType;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class OBTaskDialog
{
  private static final Logger LOG = LoggerFactory.getLogger(OBTaskDialog.class);

  private final OBControllerType controller;
  private volatile Disposable subscription;

  @FXML
  private ProgressBar progressMajor;

  @FXML
  private ProgressBar progressMinor;

  @FXML
  private TextArea textArea;

  @FXML
  private Label taskTitle;

  @FXML
  private Button cancelButton;

  private Stage dialog;

  private OBTaskDialog(
    final OBControllerType inController)
  {
    this.controller = inController;
  }

  public static OBTaskDialog create(
    final OBControllerType controller)
  {
    return new OBTaskDialog(controller);
  }

  public void show(
    final OBControllerEventTaskStarted event)
    throws IOException
  {
    OBFXThread.checkIsUIThread();

    final var taskXML = OBTaskDialog.class.getResource("task.fxml");
    final var strings = this.controller.strings();
    final var resources = strings.resourceBundle();
    final var loader = new FXMLLoader(taskXML, resources);

    loader.setController(this);
    final AnchorPane pane = loader.load();

    this.dialog = new Stage();
    this.dialog.initModality(Modality.APPLICATION_MODAL);
    this.dialog.setScene(new Scene(pane));
    this.dialog.setTitle(event.name());

    this.taskTitle.setText(event.name());
    this.progressMajor.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
    this.progressMinor.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

    this.subscription =
      this.controller.events()
        .ofType(OBControllerEventTaskProgressChanged.class)
        .subscribe(this::onTaskEventProgressChanged);

    this.dialog.setOnCloseRequest(windowEvent -> {
      try {
        LOG.debug("disposing of subscription");
        this.subscription.dispose();
      } catch (final Exception e) {
        LOG.error("subscription: ", e);
      }
    });

    this.dialog.show();
  }

  @FXML
  private void onCancelSelected()
  {
    LOG.debug("cancel selected");
  }

  private void onTaskEventProgressChanged(
    final OBControllerEventTaskProgressChanged event)
  {
    LOG.debug("OBControllerEventTaskProgressChanged");

    Platform.runLater(() -> {
      this.dialog.setTitle(event.title());
      this.taskTitle.setText(event.title());

      this.textArea.appendText(event.message());
      this.textArea.appendText("\n");

      final var newMajorOpt = event.major();
      if (newMajorOpt.isPresent()) {
        final var newMajor = newMajorOpt.getAsDouble();
        final var curMajor = this.progressMajor.getProgress();
        final var propMajor = this.progressMajor.progressProperty();

        if (curMajor > newMajor) {
          propMajor.set(newMajor);
        } else {
          final var timeline = new Timeline();
          final var keyValue = new KeyValue(
            propMajor,
            Double.valueOf(newMajor));
          final var keyFrame = new KeyFrame(new Duration(50.0), keyValue);
          timeline.getKeyFrames().add(keyFrame);
          timeline.play();
        }
      } else {
        this.progressMajor.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
      }

      final var newMinorOpt = event.minor();
      if (newMinorOpt.isPresent()) {
        final var newMinor = newMinorOpt.getAsDouble();
        final var curMinor = this.progressMinor.getProgress();
        final var propMinor = this.progressMinor.progressProperty();

        if (curMinor > newMinor) {
          propMinor.set(newMinor);
        } else {
          final var timeline = new Timeline();
          final var keyValue = new KeyValue(
            propMinor,
            Double.valueOf(newMinor));
          final var keyFrame = new KeyFrame(new Duration(50.0), keyValue);
          timeline.getKeyFrames().add(keyFrame);
          timeline.play();
        }
      } else {
        this.progressMinor.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
      }
    });
  }

  public void close()
  {
    OBFXThread.checkIsUIThread();

    this.subscription.dispose();
    this.dialog.close();
  }
}
