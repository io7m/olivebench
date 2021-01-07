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

package com.io7m.olivebench.strings;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The default application string provider.
 */

public final class OBStrings implements OBStringsType
{
  private final ResourceBundle resourceBundle;

  private OBStrings(
    final ResourceBundle inResourceBundle)
  {
    this.resourceBundle = inResourceBundle;
  }

  /**
   * Retrieve the resource bundle for the given locale.
   *
   * @param locale The locale
   *
   * @return The resource bundle
   */

  public static ResourceBundle getResourceBundle(
    final Locale locale)
  {
    return ResourceBundle.getBundle(
      "com.io7m.olivebench.strings.Strings",
      locale);
  }

  /**
   * Retrieve the resource bundle for the current locale.
   *
   * @return The resource bundle
   */

  public static ResourceBundle getResourceBundle()
  {
    return getResourceBundle(Locale.getDefault());
  }

  /**
   * Create a new string provider from the given bundle.
   *
   * @param bundle The resource bundle
   *
   * @return A string provider
   */

  public static OBStringsType of(
    final ResourceBundle bundle)
  {
    return new OBStrings(bundle);
  }

  @Override
  public ResourceBundle resourceBundle()
  {
    return this.resourceBundle;
  }

  @Override
  public String format(
    final String id,
    final Object... args)
  {
    return MessageFormat.format(this.resourceBundle.getString(id), args);
  }

  @Override
  public String channelCreated()
  {
    return this.resourceBundle.getString("event.channel.created");
  }

  @Override
  public String channelChanged()
  {
    return this.resourceBundle.getString("event.channel.changed");
  }

  @Override
  public String channelDeleted()
  {
    return this.resourceBundle.getString("event.channel.deleted");
  }

  @Override
  public String channelUndeleted()
  {
    return this.resourceBundle.getString("event.channel.undeleted");
  }

  @Override
  public String channel()
  {
    return this.resourceBundle.getString("core.channel");
  }

  @Override
  public String composition()
  {
    return this.resourceBundle.getString("core.composition");
  }

  @Override
  public String identifier()
  {
    return this.resourceBundle.getString("core.identifier");
  }

  @Override
  public String name()
  {
    return this.resourceBundle.getString("core.name");
  }

  @Override
  public String region()
  {
    return this.resourceBundle.getString("core.region");
  }

  @Override
  public String metadata()
  {
    return this.resourceBundle.getString("core.metadata");
  }

  @Override
  public String channels()
  {
    return this.resourceBundle.getString("core.channels");
  }

  @Override
  public String existingType()
  {
    return this.resourceBundle.getString("core.existingType");
  }

  @Override
  public String newType()
  {
    return this.resourceBundle.getString("core.type");
  }

  @Override
  public String noteBlock()
  {
    return this.resourceBundle.getString("core.noteBlock");
  }

  @Override
  public String errorObjectAlreadyExists()
  {
    return this.resourceBundle.getString("error.objectDuplicate");
  }

  @Override
  public String nodeAdded()
  {
    return this.resourceBundle.getString("graph.nodeAdded");
  }

  @Override
  public String nodeRemoved()
  {
    return this.resourceBundle.getString("graph.nodeRemoved");
  }

  @Override
  public String nodeModified()
  {
    return this.resourceBundle.getString("graph.nodeModified");
  }

  @Override
  public String nodeSource()
  {
    return this.resourceBundle.getString("graph.nodeSource");
  }

  @Override
  public String nodeSourceType()
  {
    return this.resourceBundle.getString("graph.nodeSourceType");
  }

  @Override
  public String nodeTarget()
  {
    return this.resourceBundle.getString("graph.nodeTarget");
  }

  @Override
  public String nodeTargetType()
  {
    return this.resourceBundle.getString("graph.nodeTargetType");
  }

  @Override
  public String controllerUndoStatusChanged()
  {
    return this.resourceBundle.getString("controller.undo.statusChanged");
  }

  @Override
  public String controllerUpdateMetadata()
  {
    return this.resourceBundle.getString("controller.task.updateMetadata");
  }

  @Override
  public String controllerChannelUpdateMetadata()
  {
    return this.resourceBundle.getString("controller.task.channelUpdateMetadata");

  }

  @Override
  public String controllerTaskFinished()
  {
    return this.resourceBundle.getString("controller.task.finished");
  }

  @Override
  public String controllerTaskStarted()
  {
    return this.resourceBundle.getString("controller.task.started");
  }

  @Override
  public String controllerNewComposition()
  {
    return this.resourceBundle.getString("controller.task.newComposition.name");
  }

  @Override
  public String controllerCloseComposition()
  {
    return this.resourceBundle.getString("controller.task.closeComposition.name");
  }

  @Override
  public String controllerCreateChannel()
  {
    return this.resourceBundle.getString("controller.task.createChannel.name");
  }

  @Override
  public String controllerCompositionStatusChanged()
  {
    return this.resourceBundle.getString(
      "controller.event.compositionStatusChanged");
  }

  @Override
  public String controllerOpenComposition()
  {
    return this.resourceBundle.getString("controller.task.openComposition.name");
  }

  @Override
  public String controllerOpenCompositionFailed()
  {
    return this.resourceBundle.getString("controller.task.openComposition.error");
  }

  @Override
  public String controllerOpenCompositionParsingFailed()
  {
    return this.resourceBundle.getString(
      "controller.task.openComposition.errorFileParse");
  }

  @Override
  public String controllerOpenCompositionParsing(final Path file)
  {
    return this.format("controller.task.openComposition.parsing", file);
  }

  @Override
  public String controllerSaveComposition()
  {
    return this.resourceBundle.getString("controller.task.saveComposition.name");
  }

  @Override
  public String controllerSaveCompositionFailed()
  {
    return this.resourceBundle.getString(
      "controller.task.saveComposition.errorFileSerialize");
  }

  @Override
  public String controllerSaveCompositionSaving(final Path file)
  {
    return this.format("controller.task.saveComposition.saving", file);
  }

  @Override
  public String regionCreated()
  {
    return this.resourceBundle.getString("event.region.created");
  }

  @Override
  public String regionChanged()
  {
    return this.resourceBundle.getString("event.region.changed");
  }

  @Override
  public String regionDeleted()
  {
    return this.resourceBundle.getString("event.region.deleted");
  }

  @Override
  public String regionUndeleted()
  {
    return this.resourceBundle.getString("event.region.undeleted");
  }


  @Override
  public String controllerCreateChannelFailed()
  {
    return this.resourceBundle.getString("controller.task.createChannel.error");
  }

  @Override
  public String controllerDeleteChannelFailed()
  {
    return this.resourceBundle.getString("controller.task.deleteChannel.error");
  }

  @Override
  public String unsavedSave()
  {
    return this.resourceBundle.getString("ui.unsaved.save");
  }

  @Override
  public String unsavedDiscard()
  {
    return this.resourceBundle.getString("ui.unsaved.discard");
  }

  @Override
  public String unsavedMessage()
  {
    return this.resourceBundle.getString("ui.unsaved.message");
  }

  @Override
  public String unsavedChangesTitle()
  {
    return this.resourceBundle.getString("ui.unsaved.title");
  }

  @Override
  public String windowTitle()
  {
    return this.format("ui.window.title");
  }

  @Override
  public String windowTitleSaved(
    final Path file)
  {
    return this.format("ui.window.title.saved", file);
  }

  @Override
  public String windowTitleUnsaved(
    final Path file)
  {
    return this.format("ui.window.title.unsaved", file);
  }

  @Override
  public String channelCreateEnter()
  {
    return this.resourceBundle.getString("ui.channelCreate.enter");
  }

  @Override
  public String metadataChanged()
  {
    return this.resourceBundle.getString("event.metadata.changed");
  }

  @Override
  public String serviceRequiredUnavailable(
    final Class<?> clazz)
  {
    return this.format(
      "services.requiredUnavailable",
      clazz.getCanonicalName()
    );
  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBStrings 0x%s]",
      Integer.toUnsignedString(System.identityHashCode(this), 16)
    );
  }
}
