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

package com.io7m.olivebench.gui;

import com.io7m.olivebench.gui.internal.OBArrangementViewController;
import com.io7m.olivebench.gui.internal.OBMainServices;
import com.io7m.olivebench.gui.internal.OBMainStrings;
import com.io7m.olivebench.gui.internal.OBMainViewController;
import com.io7m.olivebench.gui.internal.OBPatternViewController;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class OBMainApplication extends Application
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBMainApplication.class);

  public OBMainApplication()
  {

  }

  @Override
  public void start(
    final Stage stage)
    throws Exception
  {
    LOG.debug("starting application");

    final var mainServices =
      OBMainServices.create();
    final var strings =
      mainServices.requireService(OBMainStrings.class);
    final var mainXML =
      OBMainViewController.class.getResource("olivebench.fxml");
    final var loader =
      new FXMLLoader(mainXML, strings.resources());

    loader.setControllerFactory(clazz -> createController(clazz, mainServices));

    final AnchorPane pane = loader.load();
    final var controller = (OBMainViewController) loader.getController();

    controller.setStage(stage);
    controller.setServices(mainServices);

    stage.setMinWidth(320.0);
    stage.setMinHeight(240.0);
    stage.setScene(new Scene(pane));
    stage.setTitle(strings.format("programTitle"));
    stage.show();
  }

  private static Object createController(
    final Class<?> clazz,
    final OBServiceDirectoryType mainServices)
  {
    LOG.debug("createController: {}", clazz);

    if (Objects.equals(clazz, OBMainViewController.class)) {
      return new OBMainViewController();
    }
    if (Objects.equals(clazz, OBPatternViewController.class)) {
      return new OBPatternViewController(mainServices);
    }
    if (Objects.equals(clazz, OBArrangementViewController.class)) {
      return new OBArrangementViewController(mainServices);
    }

    throw new IllegalStateException(
      String.format("Unrecognized class: %s", clazz)
    );
  }
}
