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

package com.io7m.olivebench.controller;

import com.io7m.olivebench.model.graph.OBChannelMetadata;
import com.io7m.olivebench.model.graph.OBChannelType;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public final class OBTaskUpdateChannelMetadata implements OBControllerTaskType
{
  private final OBController controller;
  private final Function<OBChannelMetadata, OBChannelMetadata> updater;
  private final UUID channelId;
  private OBChannelMetadata existingMetadata;

  public OBTaskUpdateChannelMetadata(
    final OBController inController,
    final UUID inChannelId,
    final Function<OBChannelMetadata, OBChannelMetadata> inUpdater)
  {
    this.controller =
      Objects.requireNonNull(inController, "inController");
    this.channelId =
      Objects.requireNonNull(inChannelId, "inChannelId");
    this.updater =
      Objects.requireNonNull(inUpdater, "updater");
  }

  @Override
  public String name()
  {
    return this.controller.strings().controllerChannelUpdateMetadata();
  }

  @Override
  public void taskDo()
  {
    final var composition =
      this.controller.composition();
    final var channel =
      (OBChannelType) composition.graph().nodes().get(this.channelId);

    this.existingMetadata =
      channel.channelMetadata().update(this.updater);
  }

  @Override
  public UndoStyle undoStyle()
  {
    return UndoStyle.CAN_UNDO;
  }

  @Override
  public boolean isLongRunning()
  {
    return false;
  }

  @Override
  public void taskUndo()
  {
    final var composition =
      this.controller.composition();
    final var channel =
      (OBChannelType) composition.graph().nodes().get(this.channelId);

    channel.channelMetadata().set(this.existingMetadata);
  }
}
