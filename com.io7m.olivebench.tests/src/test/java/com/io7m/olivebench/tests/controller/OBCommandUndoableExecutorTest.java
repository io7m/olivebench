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

import com.io7m.olivebench.controller.api.OBCommandContextType;
import com.io7m.olivebench.controller.api.OBCommandUndoableExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.io7m.olivebench.controller.api.OBCommandUndoStyle.CANNOT_UNDO;
import static com.io7m.olivebench.controller.api.OBCommandUndoStyle.CLEARS_UNDO_STACK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class OBCommandUndoableExecutorTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBCommandUndoableExecutorTest.class);

  private OBCommandUndoableExecutor executor;
  private OBCommandContextType context;
  private LinkedList<String> execLog;

  private void showExecLog()
  {
    for (int index = 0; index < this.execLog.size(); ++index) {
      LOG.debug("[{}] {}", Integer.valueOf(index), this.execLog.get(index));
    }
  }

  @BeforeEach
  public void setup()
  {
    this.executor = new OBCommandUndoableExecutor(100);
    this.executor.setHistorySizeLimit(100);

    this.execLog = new LinkedList<String>();
    this.context = Mockito.mock(OBCommandContextType.class);
  }

  @Test
  public void testUndoRedo()
    throws Exception
  {
    final var command = new OBCommandExample(this.execLog, "X");
    assertEquals(0, command.doCount);
    assertEquals(0, command.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.execute(this.context, command);
    assertEquals(1, command.doCount);
    assertEquals(0, command.undoCount);
    assertEquals(1, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.undo(this.context);
    assertEquals(1, command.doCount);
    assertEquals(1, command.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(1, this.executor.redoStackSize());

    this.executor.redo(this.context);
    assertEquals(2, command.doCount);
    assertEquals(1, command.undoCount);
    assertEquals(1, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.undo(this.context);
    assertEquals(2, command.doCount);
    assertEquals(2, command.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(1, this.executor.redoStackSize());

    this.showExecLog();
  }

  @Test
  public void testUndoRedoNoHistory()
    throws Exception
  {
    final var c0 = new OBCommandExample(this.execLog, "X");
    final var c1 = new OBCommandExample(this.execLog, "Y");
    final var c2 = new OBCommandExample(this.execLog, "Z");

    this.executor.setHistorySizeLimit(1);
    this.executor.execute(this.context, c0);
    this.executor.execute(this.context, c1);
    this.executor.execute(this.context, c2);

    assertEquals("commandDo X", this.execLog.poll());
    assertEquals("commandDo Y", this.execLog.poll());
    assertEquals("commandDo Z", this.execLog.poll());

    assertEquals(1, c0.doCount);
    assertEquals(1, c1.doCount);
    assertEquals(1, c2.doCount);
    assertEquals(0, c0.undoCount);
    assertEquals(0, c1.undoCount);
    assertEquals(0, c2.undoCount);
    assertEquals(1, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.undo(this.context);
    assertEquals(1, c0.doCount);
    assertEquals(1, c1.doCount);
    assertEquals(1, c2.doCount);
    assertEquals(0, c0.undoCount);
    assertEquals(0, c1.undoCount);
    assertEquals(1, c2.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(1, this.executor.redoStackSize());

    this.executor.redo(this.context);
    assertEquals(1, c0.doCount);
    assertEquals(1, c1.doCount);
    assertEquals(2, c2.doCount);
    assertEquals(0, c0.undoCount);
    assertEquals(0, c1.undoCount);
    assertEquals(1, c2.undoCount);
    assertEquals(1, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.showExecLog();
  }

  @Test
  public void testUndoRedundant()
    throws Exception
  {
    final var command = new OBCommandExample(this.execLog, "X");
    assertEquals(0, command.doCount);
    assertEquals(0, command.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.execute(this.context, command);
    assertEquals(1, command.doCount);
    assertEquals(0, command.undoCount);
    assertEquals(1, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.undo(this.context);
    assertEquals(1, command.doCount);
    assertEquals(1, command.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(1, this.executor.redoStackSize());

    this.executor.undo(this.context);
    assertEquals(1, command.doCount);
    assertEquals(1, command.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(1, this.executor.redoStackSize());

    this.showExecLog();
  }

  @Test
  public void testRedoRedundant()
    throws Exception
  {
    final var command = new OBCommandExample(this.execLog, "X");
    assertEquals(0, command.doCount);
    assertEquals(0, command.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.execute(this.context, command);
    assertEquals(1, command.doCount);
    assertEquals(0, command.undoCount);
    assertEquals(1, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.undo(this.context);
    assertEquals(1, command.doCount);
    assertEquals(1, command.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(1, this.executor.redoStackSize());

    this.executor.redo(this.context);
    assertEquals(2, command.doCount);
    assertEquals(1, command.undoCount);
    assertEquals(1, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.redo(this.context);
    assertEquals(2, command.doCount);
    assertEquals(1, command.undoCount);
    assertEquals(1, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.showExecLog();
  }

  @Test
  public void testUndoNotUndoable()
    throws Exception
  {
    final var command = new OBCommandExample(this.execLog, "X", CANNOT_UNDO);
    assertEquals(0, command.doCount);
    assertEquals(0, command.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.execute(this.context, command);
    assertEquals(1, command.doCount);
    assertEquals(0, command.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.undo(this.context);
    assertEquals(1, command.doCount);
    assertEquals(0, command.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.showExecLog();
  }

  @Test
  public void testUndoClears()
    throws Exception
  {
    final var c0 = new OBCommandExample(this.execLog, "X");
    final var c1 = new OBCommandExample(this.execLog, "Y");
    final var c2 = new OBCommandExample(this.execLog, "Z", CLEARS_UNDO_STACK);

    this.executor.execute(this.context, c0);
    assertEquals(1, c0.doCount);
    assertEquals(0, c0.undoCount);
    assertEquals(1, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.execute(this.context, c1);
    assertEquals(1, c1.doCount);
    assertEquals(0, c1.undoCount);
    assertEquals(2, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.executor.execute(this.context, c2);
    assertEquals(1, c2.doCount);
    assertEquals(0, c2.undoCount);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.showExecLog();
  }

  @Test
  public void testBadHistorySize()
    throws Exception
  {
    final var ex = assertThrows(IllegalArgumentException.class, () -> {
      this.executor.setHistorySizeLimit(0);
    });

    assertTrue(ex.getMessage().contains("≥ 1"));
  }

  @Test
  public void testUndoHistoryResize()
    throws Exception
  {
    final var commands =
      IntStream.range(0, 30)
        .mapToObj(x -> new OBCommandExample(this.execLog, Integer.toString(x)))
        .collect(Collectors.toList());

    for (final var command : commands) {
      this.executor.execute(this.context, command);
    }

    assertEquals(30, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    for (final var command : commands) {
      assertEquals(1, command.doCount);
      assertEquals(0, command.undoCount);
    }

    this.executor.setHistorySizeLimit(5);
    assertEquals(5, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    for (final var ignored : commands) {
      this.executor.undo(this.context);
    }

    assertEquals(0, this.executor.undoStackSize());
    assertEquals(5, this.executor.redoStackSize());

    this.showExecLog();

    for (int index = 0; index < 30; ++index) {
      assertEquals(
        String.format("commandDo %d", Integer.valueOf(index)),
        this.execLog.poll()
      );
    }
    for (int index = 0; index < 5; ++index) {
      assertEquals(
        String.format("commandUndo %d", Integer.valueOf(29 - index)),
        this.execLog.poll()
      );
    }
    assertEquals(0, this.execLog.size());
  }

  @Test
  public void testRedoHistoryResize()
    throws Exception
  {
    final var commands =
      IntStream.range(0, 30)
        .mapToObj(x -> new OBCommandExample(this.execLog, Integer.toString(x)))
        .collect(Collectors.toList());

    for (final var command : commands) {
      this.executor.execute(this.context, command);
    }

    assertEquals(30, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    for (final var command : commands) {
      assertEquals(1, command.doCount);
      assertEquals(0, command.undoCount);
    }

    for (final var ignored : commands) {
      this.executor.undo(this.context);
    }

    this.executor.setHistorySizeLimit(5);
    assertEquals(0, this.executor.undoStackSize());
    assertEquals(5, this.executor.redoStackSize());

    for (final var ignored : commands) {
      this.executor.redo(this.context);
    }

    assertEquals(5, this.executor.undoStackSize());
    assertEquals(0, this.executor.redoStackSize());

    this.showExecLog();

    for (int index = 0; index < 30; ++index) {
      assertEquals(
        String.format("commandDo %d", Integer.valueOf(index)),
        this.execLog.poll()
      );
    }
    for (int index = 0; index < 30; ++index) {
      assertEquals(
        String.format("commandUndo %d", Integer.valueOf(29 - index)),
        this.execLog.poll()
      );
    }
    for (int index = 0; index < 5; ++index) {
      assertEquals(
        String.format("commandDo %d", Integer.valueOf(index)),
        this.execLog.poll()
      );
    }
    assertEquals(0, this.execLog.size());
  }
}
