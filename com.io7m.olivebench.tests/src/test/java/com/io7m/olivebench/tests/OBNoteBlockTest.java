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

package com.io7m.olivebench.tests;

import com.io7m.olivebench.model.blocks.OBNote;
import com.io7m.olivebench.model.blocks.OBNoteBlock;
import com.io7m.olivebench.strings.OBStrings;
import com.io7m.olivebench.strings.OBStringsType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class OBNoteBlockTest
{
  private OBStringsType strings;

  @BeforeEach
  public void testSetup()
  {
    this.strings = OBStrings.of(OBStrings.getResourceBundle());
  }

  @Test
  public void testAdd()
  {
    final var notes = new OBNoteBlock(this.strings, UUID.randomUUID());
    Assertions.assertEquals(Set.of(), notes.read());
    final var note0 = OBNote.of(0L, 0L, 0L);
    final var note1 = OBNote.of(1L, 0L, 0L);
    notes.addNote(note0);
    Assertions.assertEquals(Set.of(note0), notes.read());
    notes.addNotes(Set.of(note0, note1));
    Assertions.assertEquals(Set.of(note0, note1), notes.read());
  }

  @Test
  public void testRemove()
  {
    final var notes = new OBNoteBlock(this.strings, UUID.randomUUID());
    Assertions.assertEquals(Set.of(), notes.read());
    final var note0 = OBNote.of(0L, 0L, 0L);
    final var note1 = OBNote.of(1L, 0L, 0L);
    notes.addNotes(Set.of(note0, note1));
    Assertions.assertEquals(Set.of(note0, note1), notes.read());
    notes.removeNotes(Set.of(note0, note1));
    Assertions.assertEquals(Set.of(), notes.read());
  }

  @Test
  public void testReplace()
  {
    final var notes = new OBNoteBlock(this.strings, UUID.randomUUID());
    Assertions.assertEquals(Set.of(), notes.read());
    final var note0 = OBNote.of(0L, 0L, 0L);
    final var note1 = OBNote.of(1L, 0L, 0L);
    notes.addNote(note0);
    Assertions.assertEquals(Set.of(note0), notes.read());

    notes.replaceNotes(
      Map.ofEntries(Map.entry(note0, note1))
    );

    Assertions.assertEquals(Set.of(note1), notes.read());
  }
}
