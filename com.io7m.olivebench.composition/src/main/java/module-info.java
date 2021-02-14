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

import com.io7m.olivebench.composition.OBCompositionFactoryType;
import com.io7m.olivebench.composition.OBCompositions;

/**
 * Olivebench (Composition model)
 */

module com.io7m.olivebench.composition
{
  requires static com.io7m.immutables.style;
  requires static org.immutables.value;
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;
  requires com.io7m.jcip.annotations;

  requires transitive com.io7m.olivebench.events.api;
  requires transitive com.io7m.olivebench.services.api;

  requires transitive io.reactivex.rxjava3;
  requires transitive com.io7m.jranges.core;
  requires transitive com.io7m.jregions.core;
  requires transitive com.io7m.jtensors.core;

  requires com.io7m.jxtrand.vanilla;
  requires com.io7m.jxtrand.api;

  opens com.io7m.olivebench.composition.internal
    to com.io7m.jxtrand.vanilla;

  provides OBCompositionFactoryType with OBCompositions;

  exports com.io7m.olivebench.composition.annotations;
  exports com.io7m.olivebench.composition.ports;
  exports com.io7m.olivebench.composition.regions;
  exports com.io7m.olivebench.composition.spaces;
  exports com.io7m.olivebench.composition;
}
