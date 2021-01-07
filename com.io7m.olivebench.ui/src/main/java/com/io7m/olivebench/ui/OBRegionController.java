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

import com.io7m.olivebench.strings.OBStringsType;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Objects;

public final class OBRegionController
{
  private Pane pane;

  private OBRegionController()
  {

  }

  public static OBRegionController create(
    final OBStringsType strings)
    throws IOException
  {
    Objects.requireNonNull(strings, "strings");

    final var taskXML =
      OBRegionController.class.getResource("region.fxml");
    final var resources =
      strings.resourceBundle();
    final var loader =
      new FXMLLoader(taskXML, resources);

    final var regionController = new OBRegionController();
    loader.setController(regionController);

    final var pane = loader.load();
    regionController.pane = (Pane) pane;
    return regionController;
  }

  public Pane pane()
  {
    return this.pane;
  }
}
