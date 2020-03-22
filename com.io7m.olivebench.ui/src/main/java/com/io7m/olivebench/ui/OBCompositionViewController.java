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
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OBCompositionViewController implements OBViewControllerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBCompositionViewController.class);

  public OBCompositionViewController()
  {

  }

  @FXML
  private void onToolCursorSelected()
  {
    LOG.trace("onToolCursorSelected");
  }

  @FXML
  private void onToolDrawSelected()
  {
    LOG.trace("onToolDrawSelected");
  }

  @FXML
  private void onToolZoomSelected()
  {
    LOG.trace("onToolZoomSelected");
  }

  @Override
  public void initialize(
    final OBControllerType controller)
  {

  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBCompositionViewController 0x%s]",
      Long.toUnsignedString(System.identityHashCode(this)));
  }
}