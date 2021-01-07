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

package com.io7m.olivebench.model.properties;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public final class OBProperty<T> implements OBPropertyType<T>
{
  private final AtomicReference<T> valueRef;
  private final Subject<T> valueSubject;

  private OBProperty(
    final T initial)
  {
    this.valueRef = new AtomicReference<>(
      Objects.requireNonNull(initial, "initial")
    );
    this.valueSubject =
      BehaviorSubject.createDefault(initial)
        .toSerialized();
  }

  public static <T> OBPropertyType<T> create(
    final T initial)
  {
    return new OBProperty<>(initial);
  }

  @Override
  public T set(
    final T value)
  {
    Objects.requireNonNull(value, "value");
    final var existing = this.valueRef.getAndSet(value);
    this.valueSubject.onNext(value);
    return existing;
  }

  @Override
  public T update(
    final Function<T, T> update)
  {
    Objects.requireNonNull(update, "update");
    return this.valueRef.getAndUpdate(
      existing -> {
        final var newValue =
          Objects.requireNonNull(update.apply(existing), "Updated value");
        this.valueSubject.onNext(newValue);
        return newValue;
      }
    );
  }

  @Override
  public Observable<T> asObservable()
  {
    return this.valueSubject;
  }

  @Override
  public T read()
  {
    return this.valueRef.get();
  }

}
