package com.github.ktsr42.yajsynclib;
        
import com.github.perlundq.yajsync.RsyncServer;
import com.github.perlundq.yajsync.internal.channels.ChannelException;
import com.github.perlundq.yajsync.internal.util.ArgumentParser;
import com.github.perlundq.yajsync.internal.util.ArgumentParsingError;
import com.github.perlundq.yajsync.internal.util.Option;
import com.github.perlundq.yajsync.internal.util.Util;
import com.github.perlundq.yajsync.net.DuplexByteChannel;
import com.github.perlundq.yajsync.net.ServerChannel;
import com.github.perlundq.yajsync.net.ServerChannelFactory;
import com.github.perlundq.yajsync.net.StandardServerChannelFactory;
import com.github.perlundq.yajsync.net.StandardSocketChannel;
import com.github.perlundq.yajsync.server.module.ModuleException;
import com.github.perlundq.yajsync.server.module.ModuleProvider;
import com.github.perlundq.yajsync.server.module.Modules;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author klaas
 */
public class LibServer {
    private static final Logger _log = Logger.getLogger("RsyncServer");
    private static final int THREAD_FACTOR = 4;
    public static final int SOCKET_BACKLOG = 5;

    private CountDownLatch _isListeningLatch;
    private int _numThreads = Runtime.getRuntime().availableProcessors() * THREAD_FACTOR;
    
    private ModuleProvider _moduleProvider;
    private int _verbosity = 1;  // FIXME
    private final RsyncServer.Builder _serverBuilder = new RsyncServer.Builder();
    
    private int _timeout = 0;
    
    private String _moduleName;
    private ExecutorService _executor;
    private RsyncServer _server;
    private ServerSocketChannel _listenSock;

    public LibServer(String moduleName) {
        if(moduleName == null) _moduleName = UUID.randomUUID().toString().substring(0, 6);
        else                   _moduleName = moduleName;

        _moduleProvider = new StdModuleProvider(_moduleName);
        _executor = Executors.newFixedThreadPool(_numThreads);
        _server = _serverBuilder.build(_executor);
    }

    // Notes: start() method takes address  parameter and returns tuple: (module name and local port

    private Callable<Boolean> createCallable(final RsyncServer server,
                                             final DuplexByteChannel sock,
                                             final boolean isInterruptible)
    {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                boolean isOK = false;
                try {
                    Modules modules;
                    if (sock.peerPrincipal().isPresent()) {
                        if (_log.isLoggable(Level.FINE)) {
                            _log.fine(String.format("%s connected from %s",
                                                    sock.peerPrincipal().get(),
                                                    sock.peerAddress()));
                        }
                        modules = _moduleProvider.newAuthenticated(
                                                        sock.peerAddress(),
                                                        sock.peerPrincipal().get());
                    } else {
                        if (_log.isLoggable(Level.FINE)) {
                            _log.fine("got anonymous connection from " +
                                      sock.peerAddress());
                        }
                        modules = _moduleProvider.newAnonymous(
                                                        sock.peerAddress());
                    }
                    isOK = server.serve(modules, sock, sock, isInterruptible);
                } catch (ModuleException e) {
                    if (_log.isLoggable(Level.SEVERE)) {
                        _log.severe(String.format(
                            "Error: failed to initialise modules for " +
                            "principal %s using ModuleProvider %s: %s%n",
                                sock.peerPrincipal().get(), _moduleProvider, e));
                    }
                } catch (ChannelException e) {
                    if (_log.isLoggable(Level.SEVERE)) {
                        _log.severe("Error: communication closed with peer: " +
                                    e.getMessage());
                    }
                } catch (Throwable t) {
                    if (_log.isLoggable(Level.SEVERE)) {
                        _log.log(Level.SEVERE, "", t);
                    }
                } finally {
                    try {
                        sock.close();
                    } catch (IOException e) {
                        if (_log.isLoggable(Level.SEVERE)) {
                            _log.severe(String.format(
                                "Got error during close of socket %s: %s",
                                sock, e.getMessage()));
                        }
                    }
                }

                if (_log.isLoggable(Level.FINE)) {
                    _log.fine("Thread exit status: " + (isOK ? "OK" : "ERROR"));
                }
                return isOK;
            }
        };
    }

    public Object[] initServer(InetAddress localAddress) throws IOException {
        Level logLevel = Util.getLogLevelForNumber(Util.WARNING_LOG_LEVEL_NUM + _verbosity);
        Util.setRootLogLevel(logLevel);

        _listenSock = ServerSocketChannel.open();
        _listenSock.setOption(StandardSocketOptions.SO_REUSEADDR, true);

        _listenSock.bind(new InetSocketAddress(localAddress,0), SOCKET_BACKLOG);

        if(_isListeningLatch != null) {
            _isListeningLatch.countDown();
        }

        InetSocketAddress localaddr = (InetSocketAddress) _listenSock.getLocalAddress();

        return new Object[] {_moduleName, localaddr.getPort() };
    }

    public void run() throws InterruptedException, IOException {
        try {
            while (true) {
                SocketChannel netSock = _listenSock.accept();                   // throws IOException
                Callable<Boolean> c = createCallable(_server, new StandardSocketChannel(netSock, _timeout), true);
                _executor.submit(c);                                             // NOTE: result discarded
            }
        } finally {
            if (_log.isLoggable(Level.INFO)) {
                _log.info("shutting down...");
            }
            _executor.shutdown();
            _moduleProvider.close();
            while (!_executor.awaitTermination(5, TimeUnit.MINUTES)) {
                _log.info("some sessions are still running, waiting for them " +
                        "to finish before exiting");
            }
            if (_log.isLoggable(Level.INFO)) {
                _log.info("done");
            }
        }
    }

    public int start(InetAddress address) throws IOException, InterruptedException
    {
        Level logLevel = Util.getLogLevelForNumber(Util.WARNING_LOG_LEVEL_NUM + _verbosity);
        Util.setRootLogLevel(logLevel);

        ServerChannelFactory socketFactory = new StandardServerChannelFactory();

        socketFactory.setReuseAddress(true);
        ExecutorService executor = Executors.newFixedThreadPool(_numThreads);
        RsyncServer server = _serverBuilder.build(executor);

        try (ServerChannel listenSock = socketFactory.open(address, 0, _timeout)) {  // throws IOException
            if (_isListeningLatch != null) {
                _isListeningLatch.countDown();
            }
            while (true) {
                DuplexByteChannel sock = listenSock.accept();                   // throws IOException
                Callable<Boolean> c = createCallable(server, sock, true);
                executor.submit(c);                                             // NOTE: result discarded
            }
        } finally {
            if (_log.isLoggable(Level.INFO)) {
                _log.info("shutting down...");
            }
            executor.shutdown();
            _moduleProvider.close();
            while (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                _log.info("some sessions are still running, waiting for them " +
                          "to finish before exiting");
            }
            if (_log.isLoggable(Level.INFO)) {
                _log.info("done");
            }
        }
    }
}
