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

import com.io7m.jwheatsheaf.api.JWFileChooserAction;
import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.api.JWFileChooserFilterType;
import com.io7m.jwheatsheaf.api.JWFileChoosersType;
import com.io7m.jwheatsheaf.ui.JWFileChoosers;
import com.io7m.olivebench.controller.OBControllerEventCompositionStatusChanged;
import com.io7m.olivebench.controller.OBControllerEventTaskFailed;
import com.io7m.olivebench.controller.OBControllerEventTaskFinished;
import com.io7m.olivebench.controller.OBControllerEventTaskStarted;
import com.io7m.olivebench.controller.OBControllerType;
import com.io7m.olivebench.preferences.OBPreferencesControllerType;
import com.io7m.olivebench.services.api.OBServiceDirectoryType;
import com.io7m.olivebench.strings.OBStrings;
import com.io7m.olivebench.strings.OBStringsType;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class OBMainViewController implements Initializable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBMainViewController.class);

  private final CompositeDisposable subscriptions;
  private final ScheduledExecutorService executor;

  @FXML
  private MenuItem menuItemClose;

  @FXML
  private MenuItem menuItemSave;

  @FXML
  private MenuItem menuItemSaveAs;

  @FXML
  private MenuItem menuItemCompositionChannels;

  @FXML
  private MenuItem menuItemCompositionMetadata;

  @FXML
  private Node iconUnsaved;
  private Tooltip iconUnsavedTooltip;

  @FXML
  private Pane sectionComposition;

  /**
   * Note: the naming of this field is significant. It *must* be "sectionComposition" + "Controller"
   * in order to have the JavaFX runtime inject the field correctly.
   */

  @FXML
  private OBCompositionViewController sectionCompositionController;

  @FXML
  private Pane contentArea;

  private JWFileChoosersType fileChoosers;
  private OBTaskDialog taskDialog;
  private volatile OBControllerType controller;
  private volatile Stage stage;
  private volatile OBServiceDirectoryType mainServices;
  private OBStringsType strings;

  public OBMainViewController()
  {
    this.executor =
      Executors.newSingleThreadScheduledExecutor(r -> {
        final var thread = new Thread(r);
        thread.setName(
          String.format(
            "com.io7m.olivebench.ui.scheduled[%d]",
            Long.valueOf(thread.getId())));
        return thread;
      });

    this.subscriptions = new CompositeDisposable();
  }

  private static List<JWFileChooserFilterType> fileNameFilters()
  {
    return List.of(new OBFileChooserFilterOBX());
  }

  private static JWFileChoosersType loadFileChoosers()
  {
    return JWFileChoosers.create();
  }

  public void setStage(
    final Stage inStage)
  {
    this.stage = Objects.requireNonNull(inStage, "stage");
    this.stage.setOnCloseRequest(event -> {
      if (this.controller.unsavedChanges()) {
        event.consume();
        OBUnsavedDialog.show(
          this.controller,
          save -> this.saveAndShutDown(),
          discard -> this.shutDown());
      } else {
        this.shutDown();
      }
    });
  }

  @FXML
  private void onMenuUndoSelected()
  {
    LOG.trace("onMenuUndoSelected");
  }

  @FXML
  private void onMenuOpenSelected()
    throws MalformedURLException
  {
    LOG.trace("onMenuOpenSelected");

    final var preferences =
      this.mainServices.requireService(OBPreferencesControllerType.class);

    final var config =
      JWFileChooserConfiguration.builder()
        .setFileImageSet(new OBFileChooserIconSet())
        .setAction(JWFileChooserAction.OPEN_EXISTING_SINGLE)
        .addAllFileFilters(fileNameFilters())
        .setAllowDirectoryCreation(true)
        .setCssStylesheet(new URL(OBCSS.stylesheet()))
        .setFileSystem(FileSystems.getDefault())
        .addAllRecentFiles(
          preferences.preferences()
            .recentItems()
            .recentFiles()
        ).build();

    final var chooser = this.fileChoosers.create(this.stage, config);
    final var selected = chooser.showAndWait();
    if (!selected.isEmpty()) {
      this.controller.openComposition(selected.get(0));
    }
  }

  @FXML
  private void onMenuItemCompositionChannelsSelected()
    throws IOException
  {
    final var mainXML =
      OBMainViewController.class.getResource("channels.fxml");
    final var loader =
      new FXMLLoader(mainXML, OBStrings.getResourceBundle());

    final Pane pane = loader.load();
    final OBViewControllerType controller = loader.getController();
    controller.initialize(this.mainServices);

    final var stage = new Stage();
    stage.setScene(new Scene(pane));
    stage.setTitle(this.strings.channels());
    stage.show();
  }

  @FXML
  private void onMenuItemCompositionMetadataSelected()
    throws IOException
  {
    final var mainXML =
      OBMainViewController.class.getResource("metadata.fxml");
    final var loader =
      new FXMLLoader(mainXML, OBStrings.getResourceBundle());

    final Pane pane = loader.load();
    final OBViewControllerType controller = loader.getController();
    controller.initialize(this.mainServices);

    final var stage = new Stage();
    stage.setScene(new Scene(pane));
    stage.setTitle(this.strings.metadata());
    stage.show();
  }

  @FXML
  private void onMenuSaveAsSelected()
    throws MalformedURLException
  {
    LOG.trace("onMenuSaveAsSelected");
    this.chooseFileAndSave();
  }

  @FXML
  private void onMenuSaveSelected()
    throws MalformedURLException
  {
    LOG.trace("onMenuSaveSelected");

    final var currentFilenameOpt = this.controller.currentFilename();
    if (currentFilenameOpt.isPresent()) {
      this.controller.saveComposition();
      return;
    }

    this.chooseFileAndSave();
  }

  private void chooseFileAndSave()
    throws MalformedURLException
  {
    final var preferences =
      this.mainServices.requireService(OBPreferencesControllerType.class);

    final var config =
      JWFileChooserConfiguration.builder()
        .setFileImageSet(new OBFileChooserIconSet())
        .setAction(JWFileChooserAction.CREATE)
        .addAllFileFilters(fileNameFilters())
        .setAllowDirectoryCreation(true)
        .setCssStylesheet(new URL(OBCSS.stylesheet()))
        .setFileSystem(FileSystems.getDefault())
        .addAllRecentFiles(
          preferences.preferences()
            .recentItems()
            .recentFiles()
        ).build();

    final var chooser = this.fileChoosers.create(this.stage, config);
    final var selected = chooser.showAndWait();
    if (!selected.isEmpty()) {
      this.controller.saveAsComposition(selected.get(0));
    }
  }

  @FXML
  private void onMenuNewSelected()
  {
    LOG.trace("onMenuNewSelected");

    if (this.controller.unsavedChanges()) {
      OBUnsavedDialog.show(
        this.controller,
        save -> {
          this.controller.saveComposition()
            .thenCompose(ignored -> {
              this.controller.newComposition();
              return null;
            });
        },
        discard -> {
          this.controller.newComposition();
        }
      );
    } else {
      this.controller.newComposition();
    }
  }

  @FXML
  private void onMenuQuitSelected()
  {
    LOG.trace("onMenuQuitSelected");

    if (this.controller.unsavedChanges()) {
      OBUnsavedDialog.show(
        this.controller,
        save -> this.saveAndShutDown(),
        discard -> this.shutDown());
    } else {
      this.shutDown();
    }
  }

  @FXML
  private void onMenuCloseSelected()
  {
    LOG.trace("onMenuCloseSelected");

    if (this.controller.unsavedChanges()) {
      OBUnsavedDialog.show(
        this.controller,
        save -> this.saveAndClose(),
        discard -> this.closeCollection());
    } else {
      this.closeCollection();
    }
  }

  @FXML
  private void onPlayerControlPlayPressed()
  {
    LOG.trace("onPlayerControlPlayPressed");
  }

  @FXML
  private void onPlayerControlStopPressed()
  {
    LOG.trace("onPlayerControlStopPressed");
  }

  @FXML
  private void onPlayerControlFastForwardPressed()
  {
    LOG.trace("onPlayerControlFastForwardPressed");
  }

  @FXML
  private void onPlayerControlFastBackwardPressed()
  {
    LOG.trace("onPlayerControlFastBackwardPressed");
  }

  @FXML
  private void onPlayerControlToStartPressed()
  {
    LOG.trace("onPlayerControlToStartPressed");
  }

  @FXML
  private void onPlayerControlToEndPressed()
  {
    LOG.trace("onPlayerControlToEndPressed");
  }

  @Override
  public void initialize(
    final URL url,
    final ResourceBundle newResources)
  {
    LOG.debug("{} initialize", this);
    LOG.debug(
      "{} sectionCompositionController {}",
      this,
      this.sectionCompositionController);

    this.fileChoosers = loadFileChoosers();
    this.iconUnsaved.setVisible(false);

    this.sectionComposition.setVisible(true);
    this.contentArea.setVisible(false);
  }

  private void onControllerTaskFailed(
    final OBControllerEventTaskFailed event)
  {
    Platform.runLater(() -> {
      this.closeTaskDialog();

      LOG.debug("opening error dialog");
      final var errorDialog = OBErrorDialog.create(this.controller);
      try {
        errorDialog.show(event);
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  private void onControllerTaskFinished(
    final OBControllerEventTaskFinished event)
  {
    this.executor.schedule(() -> {
      Platform.runLater(this::closeTaskDialog);
    }, 1_00L, TimeUnit.MILLISECONDS);
  }

  private void closeTaskDialog()
  {
    final var existingDialog = this.taskDialog;
    if (existingDialog != null) {
      LOG.debug("closing task dialog");
      existingDialog.close();
      this.taskDialog = null;
    }
  }

  private void onControllerTaskStarted(
    final OBControllerEventTaskStarted event)
  {
    if (event.longRunning()) {
      Platform.runLater(() -> {
        try {
          LOG.debug("opening task dialog");
          this.taskDialog = OBTaskDialog.create(this.controller);
          this.taskDialog.show(event);
        } catch (final IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    }
  }

  private void onCompositionStatusChanged(
    final OBControllerEventCompositionStatusChanged event)
  {
    Platform.runLater(() -> {
      switch (event.statusNow()) {
        case STATUS_UNSAVED: {
          final var file =
            this.controller.currentFilename()
              .orElse(Path.of(""));
          this.stage.titleProperty()
            .setValue(this.controller.strings().windowTitleUnsaved(file));

          this.showUnsavedIcon();
          this.menuItemCompositionChannels.setDisable(false);
          this.menuItemCompositionMetadata.setDisable(false);
          this.menuItemClose.setDisable(false);
          this.menuItemSave.setDisable(false);
          this.menuItemSaveAs.setDisable(false);
          this.contentArea.setVisible(true);
          break;
        }

        case STATUS_NOT_LOADED: {
          this.stage.titleProperty()
            .setValue(this.controller.strings().windowTitle());

          this.hideUnsavedIcon();
          this.menuItemCompositionChannels.setDisable(true);
          this.menuItemCompositionMetadata.setDisable(true);
          this.menuItemClose.setDisable(true);
          this.menuItemSave.setDisable(true);
          this.menuItemSaveAs.setDisable(true);
          this.contentArea.setVisible(false);
          break;
        }

        case STATUS_SAVED: {
          final var file =
            this.controller.currentFilename()
              .orElse(Path.of(""));
          this.stage.titleProperty()
            .setValue(this.controller.strings().windowTitleSaved(file));

          this.hideUnsavedIcon();
          this.menuItemCompositionChannels.setDisable(false);
          this.menuItemCompositionMetadata.setDisable(false);
          this.menuItemClose.setDisable(false);
          this.menuItemSave.setDisable(true);
          this.menuItemSaveAs.setDisable(false);
          this.contentArea.setVisible(true);
          break;
        }
      }
    });
  }

  private void hideUnsavedIcon()
  {
    Tooltip.uninstall(this.iconUnsaved, this.iconUnsavedTooltip);
    this.iconUnsaved.setVisible(false);
  }

  private void showUnsavedIcon()
  {
    Tooltip.install(this.iconUnsaved, this.iconUnsavedTooltip);
    this.iconUnsaved.setVisible(true);
  }

  private void saveAndClose()
  {
    this.controller.saveComposition()
      .whenComplete((o, throwable) -> {
        if (throwable == null) {
          this.closeCollection();
        }
      });
  }

  private void closeCollection()
  {
    this.controller.closeComposition();
  }

  private void saveAndShutDown()
  {
    this.controller.saveComposition()
      .whenComplete((o, throwable) -> {
        if (throwable == null) {
          this.shutDown();
        }
      });
  }

  private void shutDown()
  {
    LOG.debug("exiting");
    System.exit(0);
  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBMainViewController 0x%s]",
      Long.toUnsignedString(System.identityHashCode(this)));
  }

  public void setServices(
    final OBServiceDirectoryType services)
  {
    this.mainServices =
      Objects.requireNonNull(services, "services");
    this.controller =
      services.requireService(OBControllerType.class);
    this.strings =
      services.requireService(OBStringsType.class);

    this.sectionCompositionController.initialize(services);

    this.iconUnsavedTooltip =
      new Tooltip(this.strings.unsavedMessage());

    this.subscriptions.add(
      this.controller.events()
        .ofType(OBControllerEventTaskStarted.class)
        .subscribe(this::onControllerTaskStarted)
    );
    this.subscriptions.add(
      this.controller.events()
        .ofType(OBControllerEventTaskFinished.class)
        .subscribe(this::onControllerTaskFinished)
    );
    this.subscriptions.add(
      this.controller.events()
        .ofType(OBControllerEventTaskFailed.class)
        .subscribe(this::onControllerTaskFailed)
    );
    this.subscriptions.add(
      this.controller.events()
        .ofType(OBControllerEventCompositionStatusChanged.class)
        .subscribe(this::onCompositionStatusChanged)
    );
  }
}