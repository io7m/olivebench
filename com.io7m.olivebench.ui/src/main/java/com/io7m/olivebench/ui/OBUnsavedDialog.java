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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Objects;
import java.util.function.Consumer;

public final class OBUnsavedDialog
{
  private OBUnsavedDialog()
  {

  }

  public static void show(
    final OBControllerType controller,
    final Consumer<Save> onSave,
    final Consumer<Discard> onDiscard)
  {
    final var strings =
      controller.strings();
    final var save =
      new ButtonType(strings.unsavedSave(), ButtonBar.ButtonData.OK_DONE);
    final var discard =
      new ButtonType(strings.unsavedDiscard(), ButtonBar.ButtonData.OTHER);

    final var alert =
      new Alert(
        Alert.AlertType.CONFIRMATION,
        strings.unsavedMessage(),
        discard,
        ButtonType.CANCEL,
        save);

    alert.setTitle(strings.unsavedChangesTitle());
    final var dialogPane = alert.getDialogPane();
    dialogPane.getStylesheets().add(OBCSS.stylesheet());
    dialogPane.setHeaderText(strings.unsavedChangesTitle());

    final var resultOpt = alert.showAndWait();
    if (resultOpt.isPresent()) {
      final var result = resultOpt.get();
      if (Objects.equals(result, save)) {
        onSave.accept(new Save());
      } else if (Objects.equals(result, discard)) {
        onDiscard.accept(new Discard());
      }
    }
  }

  public static final class Save
  {
    Save()
    {

    }
  }

  public static final class Discard
  {
    Discard()
    {

    }
  }
}
