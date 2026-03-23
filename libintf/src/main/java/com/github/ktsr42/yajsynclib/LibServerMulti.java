package com.github.ktsr42.yajsynclib;
        
import com.github.perlundq.yajsync.RsyncServer;
import com.github.perlundq.yajsync.internal.channels.ChannelException;
import com.github.perlundq.yajsync.internal.util.Util;
import com.github.perlundq.yajsync.net.DuplexByteChannel;
import com.github.perlundq.yajsync.net.StandardSocketChannel;
import com.github.perlundq.yajsync.server.module.ModuleException;
import com.github.perlundq.yajsync.server.module.ModuleProvider;
import com.github.perlundq.yajsync.server.module.Modules;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author klaas
 */
public class LibServerMulti {
    private static final int THREAD_FACTOR = 4;
    public static final int SOCKET_BACKLOG = 5;

    private Logger _log = Logger.getLogger("yajsync");
    private CountDownLatch _isListeningLatch;
    private int _numThreads = Runtime.getRuntime().availableProcessors() * THREAD_FACTOR;

    private ModuleProvider _moduleProvider;
    private int _verbosity = 1;  // FIXME
    private final RsyncServer.Builder _serverBuilder = new RsyncServer.Builder();

    private int _timeout = 0;

    private int _port = 0;
    private ExecutorService _executor;
    private RsyncServer _server;
    private ServerSocketChannel _listenSock;
    private Selector socketChannelSelector;

    private Thread eventLoop;

    private boolean run;

    public LibServerMulti(int port, String[] moduleNames, String[] modulePaths) { this(port, moduleNames, modulePaths, null); }

    public LibServerMulti(int port, String[] moduleNames, String[] modulePaths, String password) {
        run = true;
        _port = port;
        MultiModules mm = new MultiModules(password);
        for(int i = 0; i < moduleNames.length; i++ ) {
            mm.addModule(moduleNames[i], modulePaths[i]);
        }
        _moduleProvider = new MultiModuleProvider(mm);
        _executor = Executors.newFixedThreadPool(_numThreads);
        _server = _serverBuilder.build(_executor);
    }

    public synchronized void stop() { run = false; }

    private synchronized boolean cont() { return run; }

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
                        _log.info("Anonymous connection from " + sock.peerAddress());
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

    public int initServer(InetAddress localAddress) throws IOException {
        Level logLevel = Util.getLogLevelForNumber(Util.WARNING_LOG_LEVEL_NUM + _verbosity);
        Util.setRootLogLevel(logLevel);

        _listenSock = ServerSocketChannel.open();
        _listenSock.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        _listenSock.configureBlocking(false);

        _listenSock.bind(new InetSocketAddress(_port), SOCKET_BACKLOG);

        socketChannelSelector = Selector.open();
        _listenSock.register(socketChannelSelector, SelectionKey.OP_ACCEPT);

        if(_isListeningLatch != null) {
            _isListeningLatch.countDown();
        }

        InetSocketAddress localaddr = (InetSocketAddress) _listenSock.getLocalAddress();

        return localaddr.getPort();
    }

    public void setLogger(Logger logger) { _log = logger; }

    private class EventLoopThread extends Thread {

        @Override
        public void run() {
            try {
                _log.finest("LibServer: start of event loop");
                while (cont()) {
                    try {
                        socketChannelSelector.select(500);

                        Set<SelectionKey> selectionKeys = socketChannelSelector.selectedKeys();
                        if( ! selectionKeys.isEmpty() ) {
                            _log.finest("LibServer: incoming connection");
                            SocketChannel netSock = _listenSock.accept();                   // throws IOException
                            Callable<Boolean> c = createCallable(_server, new StandardSocketChannel(netSock, _timeout), true);
                            _log.finest("LibServer: Submitting connection to executor");
                            _executor.submit(c);                                             // NOTE: result discarded
                        }
                        selectionKeys.clear();

                    } catch (Exception e) {
                        _log.log(Level.SEVERE, "LibServer: exception from executor submission.");
                        e.printStackTrace();
                    }
                }
            } finally {
                if (_log.isLoggable(Level.INFO)) {
                    _log.info("shutting down...");
                }
                _executor.shutdown();
                _moduleProvider.close();

                try {
                    _listenSock.close();

                    while (!_executor.awaitTermination(5, TimeUnit.MINUTES)) {
                        _log.info("some sessions are still running, waiting for them " +
                          "to finish before exiting");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                }

                if (_log.isLoggable(Level.INFO)) {
                    _log.info("done");
                }
            }
        }
    }

    public void run() {
        eventLoop = new Thread(new EventLoopThread());
        eventLoop.start();
    }

    // debugging helper
    public synchronized void block() {
        // may throw an NPE - this is deliberate
        try {
            eventLoop.wait();
        } catch (InterruptedException iex) {
            // ignored
        }
    }
}
