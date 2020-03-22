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

package com.io7m.olivebench.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

public final class OBPreferences implements OBPreferencesType
{
  private final OBPreferencesUndo undo;
  private final OBPreferencesRecentItems recents;

  private OBPreferences()
  {
    this.undo = new OBPreferencesUndo();
    this.recents = new OBPreferencesRecentItems();
  }

  public static OBPreferencesType create()
  {
    return new OBPreferences();
  }

  private static int intKey(
    final Properties properties,
    final String name,
    final int defaultValue)
  {
    try {
      return Integer.parseUnsignedInt(
        properties.getProperty(
          name,
          String.valueOf(defaultValue)
        )
      );
    } catch (final Exception e) {
      return defaultValue;
    }
  }

  private static Stream<String> listKey(
    final Properties properties,
    final String name)
  {
    final var items = new ArrayList<String>();
    for (int index = 0; index < Integer.MAX_VALUE; ++index) {
      final var key = String.format("%s.%d", name, Integer.valueOf(index));
      if (properties.containsKey(key)) {
        items.add(properties.getProperty(key));
      } else {
        break;
      }
    }
    return items.stream();
  }

  @Override
  public OBPreferencesUndoType undo()
  {
    return this.undo;
  }

  @Override
  public OBPreferencesRecentItemsType recentItems()
  {
    return this.recents;
  }

  @Override
  public void save(
    final OutputStream stream)
    throws IOException
  {
    final var properties = new Properties();
    this.undo.save(properties);
    this.recents.save(properties);
    properties.storeToXML(stream, "", StandardCharsets.UTF_8);
  }

  @Override
  public void load(
    final InputStream stream)
    throws IOException
  {
    final var properties = new Properties();
    properties.loadFromXML(stream);
    this.undo.load(properties);
    this.recents.load(properties);
  }

  private static final class OBPreferencesRecentItems
    implements OBPreferencesRecentItemsType
  {
    private volatile List<Path> recentFiles = List.of();

    OBPreferencesRecentItems()
    {

    }

    @Override
    public void clearRecentFiles()
    {
      this.recentFiles = List.of();
    }

    @Override
    public void addRecentFile(
      final Path path)
    {
      final var newFiles = new ArrayList<>(this.recentFiles);
      newFiles.removeIf(file -> Objects.equals(file, path));
      newFiles.add(path);
      while (newFiles.size() > 10) {
        newFiles.remove(0);
      }
      this.recentFiles = List.copyOf(newFiles);
    }

    @Override
    public List<Path> recentFiles()
    {
      return this.recentFiles;
    }

    void save(
      final Properties properties)
    {
      final var files = this.recentFiles;
      for (int index = 0; index < files.size(); ++index) {
        properties.setProperty(
          String.format("recent.files.%d", Integer.valueOf(index)),
          files.get(index).toString()
        );
      }
    }

    void load(
      final Properties properties)
    {
      this.clearRecentFiles();
      listKey(properties, "recent.files").forEach(file -> {
        this.addRecentFile(Paths.get(file).toAbsolutePath());
      });
    }
  }

  private static final class OBPreferencesUndo implements OBPreferencesUndoType
  {
    private static final int UNDO_HISTORY_SIZE_DEFAULT = 128;
    private static final String UNDO_HISTORY_SIZE_KEY = "undo.historySize";
    private volatile int historySize = UNDO_HISTORY_SIZE_DEFAULT;

    OBPreferencesUndo()
    {

    }

    @Override
    public void setHistorySize(
      final int size)
    {
      this.historySize = Math.max(2, Math.min(size, Integer.MAX_VALUE - 1));
    }

    @Override
    public int historySize()
    {
      return this.historySize;
    }

    void save(
      final Properties properties)
    {
      properties.setProperty(
        UNDO_HISTORY_SIZE_KEY,
        Integer.toUnsignedString(this.historySize)
      );
    }

    void load(
      final Properties properties)
    {
      this.setHistorySize(
        intKey(properties, UNDO_HISTORY_SIZE_KEY, UNDO_HISTORY_SIZE_DEFAULT));
    }
  }
}
