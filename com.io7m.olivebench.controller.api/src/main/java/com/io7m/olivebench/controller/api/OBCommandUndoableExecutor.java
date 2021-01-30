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

package com.io7m.olivebench.controller.api;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.LinkedList;
import java.util.Objects;

import static com.io7m.olivebench.controller.api.OBCommandUnit.COMMAND_UNIT;

/**
 * The default implementation of the {@link OBCommandUndoableExecutorType} interface.
 */

@ThreadSafe
public final class OBCommandUndoableExecutor
  implements OBCommandUndoableExecutorType
{
  private final Object stackLock;
  @GuardedBy("stackLock")
  private final LinkedList<OBCommandType> stackUndo;
  @GuardedBy("stackLock")
  private final LinkedList<OBCommandType> stackRedo;
  private final Subject<OBCommandUnit> events;
  private volatile int historySizeLimit;

  /**
   * Construct an executor.
   *
   * @param inHistorySizeLimit The initial history size limit
   */

  public OBCommandUndoableExecutor(
    final int inHistorySizeLimit)
  {
    this.historySizeLimit = checkHistorySizeLimit(inHistorySizeLimit);
    this.stackLock = new Object();
    this.stackUndo = new LinkedList<>();
    this.stackRedo = new LinkedList<>();
    this.events =
      PublishSubject.<OBCommandUnit>create()
        .toSerialized();
  }

  private static int checkHistorySizeLimit(
    final int inHistorySizeLimit)
  {
    if (inHistorySizeLimit < 1) {
      throw new IllegalArgumentException(
        String.format(
          "History size limit %d must be ≥ 1",
          Integer.valueOf(inHistorySizeLimit)
        ));
    }
    return inHistorySizeLimit;
  }

  @Override
  public void setHistorySizeLimit(
    final int inHistorySizeLimit)
  {
    this.historySizeLimit = checkHistorySizeLimit(inHistorySizeLimit);

    synchronized (this.stackLock) {
      while (this.stackUndo.size() > this.historySizeLimit) {
        this.stackUndo.removeLast();
      }
      while (this.stackRedo.size() > this.historySizeLimit) {
        this.stackRedo.removeLast();
      }
    }

    this.events.onNext(COMMAND_UNIT);
  }

  @Override
  public Observable<OBCommandUnit> events()
  {
    return this.events;
  }

  @Override
  public void execute(
    final OBCommandContextType context,
    final OBCommandType command)
    throws OBCommandFailureException
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(command, "command");

    try {
      command.commandDo(context);
    } catch (final Exception e) {
      throw new OBCommandFailureException(e);
    }

    switch (command.description().undoStyle()) {
      case CANNOT_UNDO: {
        break;
      }
      case CAN_UNDO: {
        synchronized (this.stackLock) {
          while (this.stackUndo.size() >= this.historySizeLimit) {
            this.stackUndo.removeLast();
          }
          this.stackUndo.push(command);
        }
        this.events.onNext(COMMAND_UNIT);
        break;
      }
      case CLEARS_UNDO_STACK: {
        synchronized (this.stackLock) {
          this.stackUndo.clear();
        }
        this.events.onNext(COMMAND_UNIT);
        break;
      }
    }
  }

  @Override
  public void undo(
    final OBCommandContextType context)
    throws OBCommandFailureException
  {
    Objects.requireNonNull(context, "context");

    final OBCommandType command;
    synchronized (this.stackLock) {
      if (!this.stackUndo.isEmpty()) {
        command = this.stackUndo.pop();
      } else {
        return;
      }
    }

    this.events.onNext(COMMAND_UNIT);

    try {
      command.commandUndo(context);
    } catch (final Exception e) {
      throw new OBCommandFailureException(e);
    }

    synchronized (this.stackLock) {
      while (this.stackRedo.size() > this.historySizeLimit) {
        this.stackRedo.removeLast();
      }
      this.stackRedo.push(command);
    }

    this.events.onNext(COMMAND_UNIT);
  }

  @Override
  public int undoStackSize()
  {
    synchronized (this.stackLock) {
      return this.stackUndo.size();
    }
  }

  @Override
  public void redo(
    final OBCommandContextType context)
    throws OBCommandFailureException
  {
    Objects.requireNonNull(context, "context");

    final OBCommandType command;
    synchronized (this.stackLock) {
      if (!this.stackRedo.isEmpty()) {
        command = this.stackRedo.pop();
      } else {
        return;
      }
    }

    this.events.onNext(COMMAND_UNIT);
    this.execute(context, command);
    this.events.onNext(COMMAND_UNIT);
  }

  @Override
  public int redoStackSize()
  {
    synchronized (this.stackLock) {
      return this.stackRedo.size();
    }
  }
}
