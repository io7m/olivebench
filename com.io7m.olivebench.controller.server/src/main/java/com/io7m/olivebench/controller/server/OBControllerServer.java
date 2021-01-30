/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.olivebench.controller.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class OBControllerServer implements OBControllerServerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBControllerServer.class);

  private final ExecutorService executor;
  private final OBControllerClientInterpreterFactoryType interpreters;
  private final InetSocketAddress address;
  private final ServerSocket serverSocket;
  private final AtomicBoolean closing;
  private final ConcurrentHashMap<Socket, Socket> clientSockets;

  private OBControllerServer(
    final ExecutorService inExecutor,
    final OBControllerClientInterpreterFactoryType inInterpreters,
    final InetSocketAddress inAddress,
    final ServerSocket inServerSocket)
  {
    this.executor =
      Objects.requireNonNull(inExecutor, "executor");
    this.interpreters =
      Objects.requireNonNull(inInterpreters, "interpreters");
    this.address =
      Objects.requireNonNull(inAddress, "address");
    this.serverSocket =
      Objects.requireNonNull(inServerSocket, "inServerSocket");
    this.closing =
      new AtomicBoolean(false);
    this.clientSockets =
      new ConcurrentHashMap<>();
  }

  public static OBControllerServerType create(
    final OBControllerClientInterpreterFactoryType interpreters,
    final InetSocketAddress address)
    throws IOException
  {
    Objects.requireNonNull(interpreters, "interpreters");
    Objects.requireNonNull(address, "address");

    final var executor =
      Executors.newCachedThreadPool(runnable -> {
        final var thread = new Thread(runnable);
        thread.setName(String.format(
          "com.io7m.olivebench.controller.server.client[%d]",
          Long.valueOf(thread.getId())
        ));
        return thread;
      });

    final var serverSocket =
      new ServerSocket(address.getPort(), 10, address.getAddress());
    serverSocket.setPerformancePreferences(0, 1, 0);
    serverSocket.setReuseAddress(true);

    return new OBControllerServer(
      executor,
      interpreters,
      address,
      serverSocket
    );
  }

  @Override
  public void close()
  {
    if (this.closing.compareAndSet(false, true)) {
      this.executor.execute(() -> {
        try {
          this.serverSocket.close();
        } catch (final IOException e) {
          LOG.error("close: ", e);
        }

        for (final var socket : this.clientSockets.values()) {
          try {
            socket.close();
          } catch (final IOException e) {
            LOG.error("close: ", e);
          }
        }
      });
      this.executor.shutdown();
    }
  }

  @Override
  public CompletableFuture<Void> start()
  {
    final var future = new CompletableFuture<Void>();
    this.executor.execute(() -> {
      try {
        this.serverSocket.setSoTimeout(1_000);
      } catch (final SocketException e) {
        future.completeExceptionally(e);
        return;
      }

      LOG.info("[{}] listening", this.serverSocket.getLocalSocketAddress());
      future.complete(null);

      while (!this.closing.get()) {
        try {
          final var clientSocket = this.serverSocket.accept();
          this.clientSockets.put(clientSocket, clientSocket);
          this.executor.execute(() -> this.runForClient(clientSocket));
        } catch (final SocketTimeoutException e) {
          // Good.
        } catch (final IOException e) {
          if (!this.serverSocket.isClosed()) {
            LOG.error("accept: ", e);
          }
        }
      }
    });
    return future;
  }

  private void runForClient(
    final Socket clientSocket)
  {
    final var clientAddress =
      clientSocket.getRemoteSocketAddress();

    LOG.info("[{}] connect", clientAddress);
    try (var socket = clientSocket) {
      final var input =
        socket.getInputStream();
      final var output =
        socket.getOutputStream();
      final var reader =
        new BufferedReader(new InputStreamReader(input, UTF_8));
      final var interpreter =
        this.interpreters.create(output);

      while (!this.closing.get()) {
        final var line = reader.readLine();
        if (line == null) {
          output.flush();
          break;
        }

        try {
          interpreter.interpret(line);
        } catch (final Exception e) {
          output.write(e.getMessage().getBytes(UTF_8));
          output.write('\n');
        }
      }
    } catch (final IOException e) {
      if (!clientSocket.isClosed()) {
        LOG.error("[{}] i/o error: ", clientAddress, e);
      }
    } finally {
      this.clientSockets.remove(clientSocket);
      LOG.info("[{}] disconnect", clientAddress);
    }
  }

  @Override
  public String toString()
  {
    return String.format(
      "[OBControllerServer 0x%08x]",
      Integer.valueOf(this.hashCode())
    );
  }
}
