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

package com.io7m.olivebench.model.blocks;

import com.io7m.olivebench.model.properties.OBProperty;
import com.io7m.olivebench.model.properties.OBPropertyReadableType;
import com.io7m.olivebench.model.properties.OBPropertyType;
import com.io7m.olivebench.strings.OBStringsType;
import net.jcip.annotations.ThreadSafe;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@ThreadSafe
public final class OBNoteBlock
  extends OBAbstractBlock implements OBNoteBlockType
{
  private final Object dataLock;
  private final TreeSet<OBNote> dataWorking;
  private final OBPropertyType<Set<OBNote>> data;

  public OBNoteBlock(
    final OBStringsType inStrings,
    final UUID inId)
  {
    super(inStrings, inId);

    this.dataLock = new Object();
    this.dataWorking = new TreeSet<>();
    this.data = OBProperty.create(Set.of());
  }

  private void update(
    final Runnable updater)
  {
    final Set<OBNote> newNotes;
    synchronized (this.dataLock) {
      updater.run();
      newNotes = Set.copyOf(this.dataWorking);
    }
    this.data.set(newNotes);
  }

  public void addNotes(
    final Collection<OBNote> notes)
  {
    Objects.requireNonNull(notes, "notes");

    this.update(() -> this.dataWorking.addAll(notes));
  }

  public void addNote(
    final OBNote note)
  {
    Objects.requireNonNull(note, "note");

    this.update(() -> this.dataWorking.add(note));
  }

  public void removeNotes(
    final Collection<OBNote> notes)
  {
    Objects.requireNonNull(notes, "notes");

    this.update(() -> {
      this.dataWorking.removeAll(notes);
    });
  }

  public void replaceNotes(
    final Map<OBNote, OBNote> notes)
  {
    Objects.requireNonNull(notes, "notes");

    this.update(() -> {
      for (final var entry : notes.entrySet()) {
        final var existing = entry.getKey();
        final var replace = entry.getValue();
        this.dataWorking.remove(existing);
        this.dataWorking.add(replace);
      }
    });
  }

  @Override
  public String type()
  {
    return this.strings().noteBlock();
  }

  @Override
  public OBPropertyReadableType<Set<OBNote>> data()
  {
    return this.data;
  }

  @Override
  public Set<OBNote> read()
  {
    return this.data.read();
  }
}
