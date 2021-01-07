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

import com.io7m.olivebench.exceptions.OBException;
import com.io7m.olivebench.model.graph.OBChannelMetadata;
import com.io7m.olivebench.model.graph.OBChannelType;
import com.io7m.olivebench.model.graph.OBNodeMetadata;
import com.io7m.olivebench.model.names.OBName;

import java.util.Objects;

public final class OBTaskCreateChannel implements OBControllerTaskType
{
  private final OBController controller;
  private final OBName name;
  private OBChannelType channelCreated;

  public OBTaskCreateChannel(
    final OBController inController,
    final OBName inName)
  {
    this.controller =
      Objects.requireNonNull(inController, "inController");
    this.name =
      Objects.requireNonNull(inName, "inName");
  }

  @Override
  public String name()
  {
    return this.controller.strings().controllerCreateChannel();
  }

  @Override
  public void taskDo()
    throws OBTaskFailureException
  {
    try {
      final var composition = this.controller.composition();
      final var graph = composition.graph();

      final var created = this.channelCreated;
      if (created != null) {
        final var nodeMetadata =
          created.nodeMetadata().read();
        final var channelMetadata =
          created.channelMetadata().read();

        this.channelCreated =
          graph.createChannel(
            created.id(),
            nodeMetadata,
            channelMetadata
          );
      } else {
        this.channelCreated =
          graph.createChannel(
            OBNodeMetadata.builder()
              .setName(this.name)
              .build(),
            OBChannelMetadata.builder()
              .build()
          );
      }
    } catch (final OBException e) {
      throw new OBTaskFailureException(
        e,
        OBControllerEventTaskFailed.builder()
          .setTitle(this.controller.strings().controllerCreateChannelFailed())
          .setException(e)
          .setMessage(e.getMessage())
          .build()
      );
    }
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
    throws OBTaskFailureException
  {
    try {
      final var created = this.channelCreated;
      if (created != null) {
        this.controller.composition()
          .graph()
          .nodeDelete(created);
      }
    } catch (final OBException e) {
      throw new OBTaskFailureException(
        e,
        OBControllerEventTaskFailed.builder()
          .setTitle(this.controller.strings().controllerDeleteChannelFailed())
          .setException(e)
          .setMessage(e.getMessage())
          .build()
      );
    }
  }
}
