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

package com.io7m.olivebench.gui.internal;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Objects;

import static com.io7m.olivebench.gui.internal.OBUnsavedDialog.OBUnsavedResolution.REQUEST_CANCEL;
import static com.io7m.olivebench.gui.internal.OBUnsavedDialog.OBUnsavedResolution.REQUEST_DISCARD;
import static com.io7m.olivebench.gui.internal.OBUnsavedDialog.OBUnsavedResolution.REQUEST_SAVE;

public final class OBUnsavedDialog
{
  private OBUnsavedDialog()
  {

  }

  public static OBUnsavedResolution showAndWait(
    final OBMainStrings strings)
  {
    final var save =
      new ButtonType(strings.format("save"), ButtonBar.ButtonData.OK_DONE);
    final var discard =
      new ButtonType(strings.format("discard"), ButtonBar.ButtonData.OTHER);

    final var alert =
      new Alert(
        Alert.AlertType.CONFIRMATION,
        strings.format("saveConfirmationMessage"),
        discard,
        ButtonType.CANCEL,
        save);

    final var title = strings.format("saveConfirmationTitle");
    alert.setTitle(title);
    final var dialogPane = alert.getDialogPane();
    dialogPane.setHeaderText(title);

    final var resultOpt = alert.showAndWait();
    if (resultOpt.isPresent()) {
      final var result = resultOpt.get();
      if (Objects.equals(result, save)) {
        return REQUEST_SAVE;
      }
      if (Objects.equals(result, discard)) {
        return REQUEST_DISCARD;
      }
    }
    return REQUEST_CANCEL;
  }

  /**
   * The action to be taken with unsaved data.
   */

  public enum OBUnsavedResolution
  {
    /**
     * Save the data.
     */

    REQUEST_SAVE,

    /**
     * Discard the data.
     */

    REQUEST_DISCARD,

    /**
     * Cancel the request.
     */

    REQUEST_CANCEL
  }
}
