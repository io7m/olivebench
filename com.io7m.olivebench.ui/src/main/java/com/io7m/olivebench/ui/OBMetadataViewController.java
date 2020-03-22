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
import com.io7m.olivebench.model.OBCompositionReadableType;
import com.io7m.olivebench.model.metadata.OBMetadataProperty;
import com.io7m.olivebench.model.metadata.OBMetadatas;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public final class OBMetadataViewController implements OBViewControllerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBMetadataViewController.class);

  private final CompositeDisposable subscriptions;

  @FXML
  private TextField identifier;

  @FXML
  private TextField title;

  @FXML
  private TextField date;

  @FXML
  private TextField creator;

  @FXML
  private TextField rights;

  @FXML
  private TextField publisher;

  @FXML
  private TextField source;

  private List<TextField> fields;
  private OBControllerType controller;

  public OBMetadataViewController()
  {
    this.subscriptions = new CompositeDisposable();
  }

  private void openUpdateDialog(
    final String key)
  {
    final var value =
      this.controller.compositionSnapshot()
        .map(OBCompositionReadableType::metadata)
        .flatMap(metadata -> metadata.findValue(key))
        .orElse("");

    try {
      OBEditMetadataPropertyDialog.create(this.controller)
        .show(OBMetadataProperty.of(key, value));
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @FXML
  private void onMetadataFieldEditTitle()
  {
    this.openUpdateDialog("dc:title");
  }

  @FXML
  private void onMetadataFieldEditDate()
  {
    this.openUpdateDialog("dc:date");
  }

  @FXML
  private void onMetadataFieldCalculateDate()
  {
    this.controller.updateMetadata(metadata -> {
      return OBMetadatas.put(
        metadata,
        "dc:date",
        OffsetDateTime.now(ZoneId.of("UTC"))
          .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      );
    });
  }

  @FXML
  private void onMetadataFieldEditCreator()
  {
    this.openUpdateDialog("dc:creator");
  }

  @FXML
  private void onMetadataFieldEditRights()
  {
    this.openUpdateDialog("dc:rights");
  }

  @FXML
  private void onMetadataFieldEditPublisher()
  {
    this.openUpdateDialog("dc:publisher");
  }

  @FXML
  private void onMetadataFieldEditSource()
  {
    this.openUpdateDialog("dc:source");
  }

  @Override
  public void initialize(
    final OBControllerType inController)
  {
    this.controller =
      Objects.requireNonNull(inController, "controller");

    this.fields =
      List.of(
        this.creator,
        this.date,
        this.identifier,
        this.publisher,
        this.rights,
        this.source,
        this.title
      );

    this.subscriptions.add(
      this.controller.events()
        .ofType(OBControllerEventCompositionStatusChanged.class)
        .subscribe(this::onCompositionStatusChanged)
    );

    this.subscriptions.add(
      this.controller.events()
        .ofType(OBControllerEventCompositionChanged.class)
        .subscribe(this::onCompositionChanged)
    );
  }

  private void onCompositionChanged(
    final OBControllerEventCompositionChanged event)
  {

  }

  private void onCompositionStatusChanged(
    final OBControllerEventCompositionStatusChanged event)
  {
    Platform.runLater(() -> {
      switch (event.statusNow()) {
        case STATUS_SAVED:
        case STATUS_UNSAVED: {
          final var snapshotOpt = this.controller.compositionSnapshot();
          if (snapshotOpt.isEmpty()) {
            return;
          }

          final var snapshot = snapshotOpt.get();
          this.identifier.setText(snapshot.graph().id().toString());

          final var meta = snapshot.metadata();
          this.creator.setText(
            meta.findValueOrDefault("dc:creator", ""));
          this.date.setText(
            meta.findValueOrDefault("dc:date", ""));
          this.publisher.setText(
            meta.findValueOrDefault("dc:publisher", ""));
          this.rights.setText(
            meta.findValueOrDefault("dc:rights", ""));
          this.source.setText(
            meta.findValueOrDefault("dc:source", ""));
          this.title.setText(
            meta.findValueOrDefault("dc:title", ""));
          break;
        }

        case STATUS_NOT_LOADED: {
          for (final var field : this.fields) {
            field.clear();
          }
          break;
        }
      }
    });
  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBMetadataViewController 0x%s]",
      Long.toUnsignedString(System.identityHashCode(this)));
  }
}
