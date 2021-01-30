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

package com.io7m.olivebench.gui.internal;

import com.io7m.jwheatsheaf.api.JWFileChooserAction;
import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.api.JWFileChoosersType;
import com.io7m.jwheatsheaf.ui.JWFileChoosers;
import com.io7m.olivebench.composition.OBCompositionEventType;
import com.io7m.olivebench.composition.OBCompositionModifiedEvent;
import com.io7m.olivebench.controller.api.OBControllerAsynchronousType;
import com.io7m.olivebench.controller.api.OBControllerCommandEvent;
import com.io7m.olivebench.controller.api.OBControllerCommandFailedEvent;
import com.io7m.olivebench.controller.api.OBControllerCompositionEvent;
import com.io7m.olivebench.controller.api.OBControllerEventType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.io7m.olivebench.gui.internal.OBUnsavedResolution.REQUEST_CANCEL;
import static com.io7m.olivebench.gui.internal.OBUnsavedResolution.REQUEST_DISCARD;

public final class OBMainViewController implements Initializable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBMainViewController.class);

  private final JWFileChoosersType fileChoosers;
  private final CompositeDisposable compositionSubscriptions;
  private OBServiceDirectoryType mainServices;
  private OBControllerAsynchronousType controller;
  private Stage stage;
  private OBMainStrings strings;

  @FXML
  private Pane rootPane;
  @FXML
  private TabPane compositionPane;
  @FXML
  private MenuItem editUndo;
  @FXML
  private MenuItem editRedo;
  @FXML
  private MenuItem fileNew;
  @FXML
  private MenuItem fileClose;
  @FXML
  private MenuItem fileSave;
  @FXML
  private MenuItem fileSaveAs;
  @FXML
  private MenuItem fileQuit;

  public OBMainViewController()
  {
    this.fileChoosers =
      JWFileChoosers.create();
    this.compositionSubscriptions =
      new CompositeDisposable();
  }

  @FXML
  private boolean onFileRequestNew()
  {
    if (this.controller.isUnsaved()) {
      if (!this.onFileRequestSave()) {
        return false;
      }
      this.controller.compositionClose();
    }

    this.controller.compositionNew();
    return true;
  }

  @FXML
  private void onFileRequestOpen()
  {
    final var chooser =
      this.fileChoosers.create(
        this.stage,
        JWFileChooserConfiguration.builder()
          .setAction(JWFileChooserAction.OPEN_EXISTING_SINGLE)
          .setAllowDirectoryCreation(false)
          .setFileSystem(FileSystems.getDefault())
          .addFileFilters(new OBFileFilter(this.strings))
          .build()
      );

    final var files = chooser.showAndWait();
    if (!files.isEmpty()) {
      this.controller.compositionOpen(files.get(0));
    }
  }

  @FXML
  private boolean onFileRequestSaveAs()
  {
    final var chooser =
      this.fileChoosers.create(
        this.stage,
        JWFileChooserConfiguration.builder()
          .setAction(JWFileChooserAction.CREATE)
          .setAllowDirectoryCreation(true)
          .setFileSystem(FileSystems.getDefault())
          .addFileFilters(new OBFileFilter(this.strings))
          .build()
      );

    final var files = chooser.showAndWait();
    if (!files.isEmpty()) {
      this.controller.compositionSave(files.get(0));
      return true;
    }
    return false;
  }

  @FXML
  private boolean onFileRequestSave()
  {
    final var file = this.controller.compositionFile();
    if (file.isPresent()) {
      this.controller.compositionSave(file.get());
      return true;
    }
    return this.onFileRequestSaveAs();
  }

  @FXML
  private OBUnsavedResolution onFileRequestClose()
  {
    if (this.controller.isUnsaved()) {
      final var save =
        OBUnsavedDialog.showAndWait(this.strings);

      switch (save) {
        case REQUEST_SAVE: {
          final var fileOpt = this.controller.compositionFile();
          if (fileOpt.isPresent()) {
            return this.onFileRequestSave() ? save : REQUEST_CANCEL;
          }
          return this.onFileRequestSaveAs() ? save : REQUEST_CANCEL;
        }
        case REQUEST_DISCARD: {
          this.controller.compositionClose();
          return save;
        }
        case REQUEST_CANCEL: {
          return save;
        }
      }
    }
    return REQUEST_DISCARD;
  }

  @FXML
  private void onFileRequestQuit()
  {
    final var resolution = this.onFileRequestClose();
    switch (resolution) {
      case REQUEST_SAVE:
      case REQUEST_DISCARD: {
        LOG.info("shutting down");
        try {
          this.mainServices.close();
        } catch (final IOException e) {
          LOG.error("i/o error: ", e);
        }
        Platform.exit();
        break;
      }
      case REQUEST_CANCEL:
        break;
    }
  }

  public void setServices(
    final OBServiceDirectoryType inMainServices)
  {
    this.mainServices =
      Objects.requireNonNull(inMainServices, "mainServices");
    this.strings =
      this.mainServices.requireService(OBMainStrings.class);
    this.controller =
      this.mainServices.requireService(OBControllerAsynchronousType.class);

    this.controller.events()
      .subscribe(this::onControllerEvent);
  }

  private void onControllerEvent(
    final OBControllerEventType event)
  {
    if (event instanceof OBControllerCommandFailedEvent) {
      this.onControllerEventCommandFailed(
        (OBControllerCommandFailedEvent) event);
    } else if (event instanceof OBControllerCommandEvent) {
      this.onControllerEventCommand(
        (OBControllerCommandEvent) event);
    } else if (event instanceof OBControllerCompositionEvent) {
      this.onControllerCompositionEvent(
        (OBControllerCompositionEvent) event);
    } else {
      throw new IllegalStateException();
    }
  }

  private void onControllerEventCommand(
    final OBControllerCommandEvent event)
  {
    Platform.runLater(() -> {
      switch (event.kind()) {
        case COMMAND_STARTED: {
          final var command = event.command();
          this.rootPane.setDisable(command.isLongRunning());
          break;
        }
        case COMMAND_ENDED: {
          this.rootPane.setDisable(false);
          break;
        }
      }
    });
  }

  private void onControllerCompositionEvent(
    final OBControllerCompositionEvent event)
  {
    Platform.runLater(() -> {
      switch (event.kind()) {
        case COMPOSITION_OPENED: {
          final var composition =
            this.controller.composition()
              .orElseThrow(IllegalStateException::new);

          this.fileClose.setDisable(false);
          this.fileSaveAs.setDisable(false);
          this.compositionPane.setVisible(true);
          this.compositionSubscriptions.add(
            composition.events().subscribe(this::onCompositionEvent)
          );

          this.stage.setTitle(this.determineWindowTitle(
            this.controller.compositionFile(),
            this.controller.isUnsaved())
          );
          break;
        }

        case COMPOSITION_UNDO_CHANGED: {
          this.editRedo.setDisable(!this.controller.canRedo());
          this.editUndo.setDisable(!this.controller.canUndo());
          break;
        }

        case COMPOSITION_CLOSED: {
          this.fileClose.setDisable(true);
          this.fileSaveAs.setDisable(true);
          this.compositionPane.setVisible(false);
          this.compositionSubscriptions.dispose();

          this.stage.setTitle(this.determineWindowTitle(
            this.controller.compositionFile(),
            this.controller.isUnsaved())
          );
          break;
        }
      }
    });
  }

  private void onCompositionEvent(
    final OBCompositionEventType event)
  {
    if (event instanceof OBCompositionModifiedEvent) {
      this.onCompositionModifiedEvent((OBCompositionModifiedEvent) event);
    } else {
      throw new IllegalStateException();
    }
  }

  private void onCompositionModifiedEvent(
    final OBCompositionModifiedEvent event)
  {
    Platform.runLater(() -> {
      final var compositionFile =
        this.controller.compositionFile();
      final var hasUnsaved =
        this.controller.isUnsaved();

      this.fileSave.setDisable(!hasUnsaved);
      this.stage.setTitle(
        this.determineWindowTitle(compositionFile, hasUnsaved)
      );
    });
  }

  private String determineWindowTitle(
    final Optional<Path> compositionFile,
    final boolean hasUnsaved)
  {
    if (compositionFile.isPresent()) {
      if (hasUnsaved) {
        return this.strings.format(
          "programTitleWithFileUnsaved",
          compositionFile.get());
      }
      return this.strings.format("programTitleWithFile", compositionFile.get());
    }

    if (hasUnsaved) {
      return this.strings.format("programTitleUnsaved");
    }
    return this.strings.format("programTitle");
  }

  private void onControllerEventCommandFailed(
    final OBControllerCommandFailedEvent event)
  {
    event.exception().ifPresent(ex -> {
      LOG.error("controller command failed: ", ex);
    });

    Platform.runLater(() -> {
      try {
        OBErrorDialog.create(this.mainServices)
          .show(event);
      } catch (final IOException e) {
        LOG.error("unable to show error dialog: ", e);
      }
    });
  }

  public void setStage(
    final Stage inStage)
  {
    this.stage =
      Objects.requireNonNull(inStage, "stage");
  }

  @Override
  public void initialize(
    final URL url,
    final ResourceBundle resourceBundle)
  {
    this.compositionPane.setVisible(false);
  }
}
