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

package com.io7m.olivebench.main;

import com.io7m.olivebench.controller.OBControllerType;
import com.io7m.olivebench.strings.OBStrings;
import com.io7m.olivebench.ui.OBMainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * The main application.
 */

public final class MainApplication extends Application
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MainApplication.class);

  private final Optional<Path> openFile;

  public MainApplication(
    final Optional<Path> inOpenFile)
  {
    this.openFile = Objects.requireNonNull(inOpenFile, "openFile");
  }

  @Override
  public void start(final Stage stage)
    throws Exception
  {
    LOG.debug("starting application");

    final var mainServices =
      MainServices.create();
    final var mainXML =
      OBMainViewController.class.getResource("olivebench.fxml");
    final var loader =
      new FXMLLoader(mainXML, OBStrings.getResourceBundle());

    final AnchorPane pane = loader.load();
    final var controller = (OBMainViewController) loader.getController();

    stage.setMinWidth(320.0);
    stage.setMinHeight(240.0);
    stage.setScene(new Scene(pane));
    stage.titleProperty().setValue("Olivebench");
    stage.show();

    controller.setServices(mainServices);
    controller.setStage(stage);

    this.openFile.ifPresent(path -> {
      mainServices.requireService(OBControllerType.class)
        .openComposition(path);
    });
  }
}
