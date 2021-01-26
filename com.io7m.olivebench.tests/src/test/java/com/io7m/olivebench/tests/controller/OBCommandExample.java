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

package com.io7m.olivebench.tests.controller;

import com.io7m.olivebench.controller.api.OBCommandAbstract;
import com.io7m.olivebench.controller.api.OBCommandContextType;
import com.io7m.olivebench.controller.api.OBCommandUndoStyle;

import java.util.Objects;
import java.util.Queue;

public final class OBCommandExample extends OBCommandAbstract
{
  private final Queue<String> log;
  public int doCount;
  public int undoCount;

  public OBCommandExample(
    final String inDescription,
    final OBCommandUndoStyle inUndoStyle,
    final boolean inIsLongRunning,
    final Queue<String> inLog)
  {
    super(inDescription, inUndoStyle, inIsLongRunning);
    this.log = Objects.requireNonNull(inLog, "log");
  }

  public OBCommandExample(
    final Queue<String> inLog,
    final String name)
  {
    this(name, OBCommandUndoStyle.CAN_UNDO, false, inLog);
  }

  public OBCommandExample(
    final Queue<String> inLog,
    final String name,
    final OBCommandUndoStyle inUndoStyle)
  {
    this(name, inUndoStyle, false, inLog);
  }

  @Override
  public void commandDo(
    final OBCommandContextType context)
  {
    ++this.doCount;
    this.log.add(String.format("commandDo %s", this.description()));
  }

  @Override
  public void commandUndo(
    final OBCommandContextType context)
  {
    ++this.undoCount;
    this.log.add(String.format("commandUndo %s", this.description()));
  }
}