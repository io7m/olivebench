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

import com.io7m.olivebench.controller.OBControllerEventCompositionChanged;
import com.io7m.olivebench.controller.OBControllerEventCompositionStatusChanged;
import com.io7m.olivebench.controller.OBControllerType;
import com.io7m.olivebench.model.OBChannelEventType;
import com.io7m.olivebench.model.graph.OBChannelType;
import com.io7m.olivebench.model.names.OBName;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.strings.OBStringsType;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public final class OBChannelsViewController implements OBViewControllerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBChannelsViewController.class);

  private final CompositeDisposable subscriptions;
  private OBControllerType controller;
  private OBStringsType strings;
  private Disposable eventSub;

  @FXML
  private ListView<OBChannelType> channels;

  public OBChannelsViewController()
  {
    this.subscriptions = new CompositeDisposable();
  }

  @FXML
  private void onAddChannelSelected()
  {
    final var dialog = new TextInputDialog();
    dialog.getDialogPane().getStylesheets().add(OBCSS.stylesheet());
    dialog.setTitle(this.strings.controllerCreateChannel());
    dialog.setHeaderText(null);
    dialog.setContentText(this.strings.channelCreateEnter());

    final var nameOpt = dialog.showAndWait();
    if (nameOpt.isPresent()) {
      final var name = nameOpt.get();
      this.controller.createChannel(OBName.of(name));
    }
  }

  @FXML
  private void onRemoveChannelSelected()
  {

  }

  @Override
  public void initialize(
    final OBServiceDirectoryType services)
  {
    this.controller =
      services.requireService(OBControllerType.class);
    this.strings =
      services.requireService(OBStringsType.class);

    this.channels.setCellFactory(
      listView -> OBChannelListCell.newInstance(this.controller, this.strings)
    );

    this.subscriptions.add(
      this.controller.events()
        .ofType(OBControllerEventCompositionChanged.class)
        .map(OBControllerEventCompositionChanged::event)
        .ofType(OBChannelEventType.class)
        .subscribe(this::onChannelEvent)
    );

    this.subscriptions.add(
      this.controller.events()
        .ofType(OBControllerEventCompositionStatusChanged.class)
        .subscribe(this::onCompositionStatusEvent)
    );

    this.updateFromSnapshot();
  }

  private void onCompositionStatusEvent(
    final OBControllerEventCompositionStatusChanged event)
  {
    this.updateFromSnapshot();
  }

  private void onChannelEvent(
    final OBChannelEventType event)
  {
    this.updateFromSnapshot();
  }

  private void updateFromSnapshot()
  {
    Platform.runLater(() -> {
      final var compositionOpt = this.controller.compositionSnapshot();
      if (compositionOpt.isPresent()) {
        final var composition = compositionOpt.get();
        final var channelList =
          FXCollections.observableList(
            composition.graph()
              .nodes()
              .values()
              .stream()
              .filter(node -> node instanceof OBChannelType)
              .map(node -> (OBChannelType) node)
              .collect(Collectors.toList())
          );
        this.channels.setItems(channelList);
      } else {
        this.channels.setItems(FXCollections.emptyObservableList());
      }
    });
  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBChannelsViewController 0x%s]",
      Long.toUnsignedString(System.identityHashCode(this)));
  }
}
