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

import com.io7m.jade.api.ApplicationDirectories;
import com.io7m.jade.spi.ApplicationDirectoryConfiguration;
import com.io7m.olivebench.composition_parser.api.OBCompositionParsers;
import com.io7m.olivebench.composition_serializer.api.OBCompositionSerializers;
import com.io7m.olivebench.controller.OBController;
import com.io7m.olivebench.controller.OBControllerEventCompositionStatusChanged;
import com.io7m.olivebench.controller.OBControllerEventTaskFailed;
import com.io7m.olivebench.controller.OBControllerEventTaskFinished;
import com.io7m.olivebench.controller.OBControllerEventTaskStarted;
import com.io7m.olivebench.controller.OBControllerType;
import com.io7m.olivebench.preferences.OBPreferencesController;
import com.io7m.olivebench.preferences.OBPreferencesControllerType;
import com.io7m.olivebench.strings.OBStrings;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
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

  private final ScheduledExecutorService executor;
  private final CompositeDisposable subscriptions;
  private volatile OBControllerType controller;
  private volatile Stage stage;

  @FXML
  private MenuItem menuItemClose;

  @FXML
  private MenuItem menuItemSave;

  @FXML
  private MenuItem menuItemSaveAs;

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
  private Pane sectionChannels;

  /**
   * Note: the naming of this field is significant. It *must* be "sectionChannels" + "Controller"
   * in order to have the JavaFX runtime inject the field correctly.
   */

  @FXML
  private OBChannelsViewController sectionChannelsController;

  @FXML
  private Pane sectionMetadata;

  /**
   * Note: the naming of this field is significant. It *must* be "sectionMetadata" + "Controller"
   * in order to have the JavaFX runtime inject the field correctly.
   */

  @FXML
  private OBMetadataViewController sectionMetadataController;

  @FXML
  private Pane contentArea;

  private List<Pane> sections;
  private List<OBViewControllerType> viewControllers;
  private OBTaskDialog taskDialog;
  private OBPreferencesControllerType preferencesController;

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
  {
    LOG.trace("onMenuOpenSelected");

    final var chooser = new FileChooser();
    chooser.setTitle(this.controller.strings().controllerOpenComposition());

    final var filters = chooser.getExtensionFilters();
    filters.clear();
    filters.addAll(fileNameFilters());

    final var selectedFile = chooser.showOpenDialog(this.stage);
    if (selectedFile != null) {
      this.controller.openComposition(selectedFile.toPath());
    }
  }

  private static List<FileChooser.ExtensionFilter> fileNameFilters()
  {
    return List.of(
      new FileChooser.ExtensionFilter(
        "Olivebench XML (*.obx)",
        "*.obx"
      ),
      new FileChooser.ExtensionFilter(
        "All files",
        "*"
      )
    );
  }

  @FXML
  private void onMenuSaveAsSelected()
  {
    LOG.trace("onMenuSaveAsSelected");
    this.chooseFileAndSave();
  }

  @FXML
  private void onMenuSaveSelected()
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
  {
    final var chooser = new FileChooser();

    final var filters = chooser.getExtensionFilters();
    filters.clear();
    filters.addAll(fileNameFilters());

    chooser.setTitle(this.controller.strings().controllerSaveComposition());
    chooser.setInitialFileName("composition.obx");

    final var selectedFile = chooser.showSaveDialog(this.stage);
    if (selectedFile != null) {
      this.controller.saveAsComposition(selectedFile.toPath());
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

  @FXML
  private void onSectionCompositionSelected()
  {
    LOG.trace("onSectionCompositionSelected");
    this.hideAllSections();
    this.sectionComposition.setVisible(true);
  }

  @FXML
  private void onSectionChannelsSelected()
  {
    LOG.trace("onSectionChannelsSelected");
    this.hideAllSections();
    this.sectionChannels.setVisible(true);
  }

  @FXML
  private void onSectionMetadataSelected()
  {
    LOG.trace("onSectionMetadataSelected");
    this.hideAllSections();
    this.sectionMetadata.setVisible(true);
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
    LOG.debug(
      "{} sectionChannelsController {}",
      this,
      this.sectionChannelsController);
    LOG.debug(
      "{} sectionMetadataController {}",
      this,
      this.sectionMetadataController);

    this.viewControllers =
      List.of(
        this.sectionCompositionController,
        this.sectionChannelsController,
        this.sectionMetadataController
      );

    this.startPreferencesController();

    this.controller =
      OBController.create(
        OBStrings.of(newResources),
        OBCompositionParsers.create(),
        OBCompositionSerializers.create(),
        this.preferencesController
      );

    for (final var viewController : this.viewControllers) {
      viewController.initialize(this.controller);
    }

    this.iconUnsavedTooltip =
      new Tooltip(this.controller.strings().unsavedMessage());
    this.iconUnsaved.setVisible(false);

    this.sections =
      List.of(
        this.sectionComposition,
        this.sectionChannels,
        this.sectionMetadata
      );

    this.hideAllSections();
    this.sectionComposition.setVisible(true);
    this.contentArea.setVisible(false);

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

  private void startPreferencesController()
  {
    final var configuration =
      ApplicationDirectoryConfiguration.builder()
        .setApplicationName("com.io7m.olivebench")
        .setPortablePropertyName("com.io7m.olivebench.portable")
        .build();

    final var directories =
      ApplicationDirectories.get(configuration);
    final var configurationDirectory =
      directories.configurationDirectory();
    final var configurationFile =
      configurationDirectory.resolve("preferences.xml");
    final var configurationFileTmp =
      configurationDirectory.resolve("preferences.xml.tmp");

    LOG.info("preferences: {}", configurationFile);

    this.preferencesController =
      OBPreferencesController.create(configurationFile, configurationFileTmp);
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

  private void hideAllSections()
  {
    for (final var section : this.sections) {
      section.setVisible(false);
    }
  }

  private void onCompositionStatusChanged(
    final OBControllerEventCompositionStatusChanged event)
  {
    Platform.runLater(() -> {
      switch (event.statusNow()) {
        case STATUS_UNSAVED:
          this.showUnsavedIcon();
          this.menuItemClose.setDisable(false);
          this.menuItemSave.setDisable(false);
          this.menuItemSaveAs.setDisable(false);
          this.contentArea.setVisible(true);
          break;

        case STATUS_NOT_LOADED:
          this.hideUnsavedIcon();
          this.menuItemClose.setDisable(true);
          this.menuItemSave.setDisable(true);
          this.menuItemSaveAs.setDisable(true);
          this.contentArea.setVisible(false);
          break;

        case STATUS_SAVED:
          this.hideUnsavedIcon();
          this.menuItemClose.setDisable(false);
          this.menuItemSave.setDisable(true);
          this.menuItemSaveAs.setDisable(false);
          this.contentArea.setVisible(true);
          break;
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
}