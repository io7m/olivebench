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

import net.jcip.annotations.ThreadSafe;

/**
 * The type of executors that support undoing operations.
 *
 * Implementations are required to be thread-safe.
 */

@ThreadSafe
public interface OBCommandUndoableExecutorType
{
  /**
   * Execute the given command.
   *
   * @param context The execution context
   * @param command The command
   *
   * @throws OBCommandFailureException On command failures
   */

  void execute(
    OBCommandContextType context,
    OBCommandType command)
    throws OBCommandFailureException;

  /**
   * Undo the command on the top of the undo stack, and move it to the redo
   * stack (assuming the command can be undone).
   *
   * @param context The execution context
   *
   * @throws OBCommandFailureException On command failures
   */

  void undo(
    OBCommandContextType context)
    throws OBCommandFailureException;

  /**
   * @return The size of the current undo stack
   */

  int undoStackSize();

  /**
   * Redo the command on the top of the redo stack, and move it to the undo
   * stack (assuming the command can be undone).
   *
   * @param context The execution context
   *
   * @throws OBCommandFailureException On command failures
   */

  void redo(
    OBCommandContextType context)
    throws OBCommandFailureException;

  /**
   * @return The size of the current redo stack
   */

  int redoStackSize();

  /**
   * Set the maximum size of the undo stack, removing the oldest entries if
   * the new limit is smaller than the old limit.
   *
   * @param inHistorySizeLimit The new limit
   */

  void setHistorySizeLimit(
    int inHistorySizeLimit);
}
