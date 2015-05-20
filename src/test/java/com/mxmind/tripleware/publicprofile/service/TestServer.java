package com.mxmind.tripleware.publicprofile.service;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.*;
import org.apache.http.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

public class TestServer {

    private final static Logger LOG = LoggerFactory.getLogger(ReceivePictureTestCase.class);
    /**
     * The local address to bind to.
     * The host is an IP number rather than "localhost" to avoid surprises
     * on hosts that map "localhost" to an IPv6 address or something else.
     * The port is 0 to let the system pick one.
     */
    public final static InetSocketAddress TEST_SERVER_ADDR = new InetSocketAddress("127.0.0.1", 0);

    /**
     * The request handler registry.
     */
    private final HttpRequestHandlerRegistry handlerRegistry;

    /**
     * The server-side connection re-use strategy.
     */
    private final ConnectionReuseStrategy reuseStrategy;

    /**
     * The HTTP processor.
     * If the interceptors are thread safe and the list is not
     * modified during operation, the processor is thread safe.
     */
    private final BasicHttpProcessor httpProcessor;

    /**
     * The server parameters.
     */
    private final HttpParams serverParams;

    /**
     * Optional SSL context
     */
    private final SSLContext sslcontext;

    /**
     * The server socket, while being served.
     */
    protected volatile ServerSocket servicedSocket;

    /**
     * The request listening thread, while listening.
     */
    protected volatile Thread listenerThread;

    /**
     * The number of connections this accepted.
     */
    private final AtomicInteger connections = new AtomicInteger(0);

    public TestServer(BasicHttpProcessor processor, ConnectionReuseStrategy strategy, HttpParams params, SSLContext ssl) {
        super();
        this.handlerRegistry = new HttpRequestHandlerRegistry();
        this.reuseStrategy = (strategy != null) ? strategy : newConnectionReuseStrategy();
        this.httpProcessor = (processor != null) ? processor : newProcessor();
        this.serverParams = (params != null) ? params : newDefaultParams();
        this.sslcontext = ssl;
    }

    public TestServer(BasicHttpProcessor processor, HttpParams params) {
        this(processor, null, params, null);
    }

    public TestServer() {
        this(null, null);
    }

    protected BasicHttpProcessor newProcessor() {
        BasicHttpProcessor processor = new BasicHttpProcessor();
        processor.addInterceptor(new ResponseDate());
        processor.addInterceptor(new ResponseServer());
        processor.addInterceptor(new ResponseContent());
        processor.addInterceptor(new ResponseConnControl());

        return processor;
    }

    protected HttpParams newDefaultParams() {
        HttpParams params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 60000)
              .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
              .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
              .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
              .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "TestServer/1.1");

        return params;
    }

    protected ConnectionReuseStrategy newConnectionReuseStrategy() {
        return new DefaultConnectionReuseStrategy();
    }

    /**
     * Returns the number of connections this test server has accepted.
     */
    public int getAcceptedConnectionCount() {
        return connections.get();
    }

    public void registerDefaultHandlers() {
        //handlerRegistry.register("/echo/*", new EchoHandler());
        //handlerRegistry.register("/random/*", new RandomHandler());
    }

    /**
     * Registers a handler with the local registry.
     *
     * @param pattern the URL pattern to match
     * @param handler the handler to apply
     */
    public void register(String pattern, HttpRequestHandler handler) {
        handlerRegistry.register(pattern, handler);
    }

    /**
     * Unregisters a handler from the local registry.
     *
     * @param pattern the URL pattern
     */
    public void unregister(String pattern) {
        handlerRegistry.unregister(pattern);
    }

    /**
     * Starts this test server.
     * Use {@link #getServicePort getServicePort}
     * to obtain the port number afterwards.
     */
    public void start() throws Exception {
        if (servicedSocket != null){
            throw new IllegalStateException(String.format("%s already running", this));
        }

        ServerSocket socket;
        if (sslcontext != null) {
            SSLServerSocketFactory factory = sslcontext.getServerSocketFactory();
            socket = factory.createServerSocket();
        } else {
            socket = new ServerSocket();
        }

        socket.setReuseAddress(true);
        socket.bind(TEST_SERVER_ADDR);
        servicedSocket = socket;

        listenerThread = new Thread(new RequestListener());
        listenerThread.setDaemon(false);
        listenerThread.start();
    }

    /**
     * Stops this test server.
     */
    public void stop() throws Exception {
        if (servicedSocket != null) {
            try {
                servicedSocket.close();
            } catch (IOException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("error stopping %s", this), ex);
                }
            } finally {
                servicedSocket = null;
            }

            if (listenerThread != null) {
                listenerThread.interrupt();
            }
        }
    }

    public void awaitTermination(long timeMs) throws InterruptedException {
        if (listenerThread != null) {
            listenerThread.join(timeMs);
        }
    }

    @Override
    public String toString() {
        ServerSocket socket = servicedSocket; // avoid synchronization
        StringJoiner joiner = new StringJoiner("/");
        String status = socket == null ? "stopped" : socket.getLocalSocketAddress().toString();

        return joiner.add(this.getClass().getSimpleName()).add(status).toString();
    }

    /**
     * Obtains the port this server is servicing.
     *
     * @return the service port
     */
    public int getServicePort() {
        ServerSocket socket = servicedSocket; // avoid synchronization
        if (socket == null){
            throw new IllegalStateException("not running");
        }
        return socket.getLocalPort();
    }


    /**
     * Obtains the hostname of the server.
     *
     * @return the hostname
     */
    public String getServiceHostName() {
        ServerSocket socket = servicedSocket; // avoid synchronization
        if (socket == null){
            throw new IllegalStateException("not running");
        }
        return ((InetSocketAddress) socket.getLocalSocketAddress()).getHostName();
    }


    /**
     * Obtains the local address the server is listening on
     *
     * @return the service address
     */
    public SocketAddress getServiceAddress() {
        ServerSocket socket = servicedSocket; // avoid synchronization
        if (socket == null){
            throw new IllegalStateException("not running");
        }
        return socket.getLocalSocketAddress();
    }

    /**
     * The request listener.
     * Accepts incoming connections and launches a service thread.
     */
    protected class RequestListener implements Runnable {

        private Set<Thread> workers = Collections.synchronizedSet(new HashSet<>());

        public void run() {
            try {
                while ((servicedSocket != null) && (listenerThread == Thread.currentThread()) && !Thread.interrupted()) {
                    try {
                        accept();
                    } catch (Exception ex) {
                        ServerSocket ssock = servicedSocket;
                        if (ssock != null && !ssock.isClosed()) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(String.format("%s could not accept", TestServer.this), ex);
                            }
                        }
                        // otherwise, throttle ex
                        break;
                    }
                }
            } finally {
                cleanup();
            }
        }

        protected void accept() throws IOException {
            // Set up HTTP connection
            Socket socket = servicedSocket.accept();
            connections.incrementAndGet();
            DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
            connection.bind(socket, serverParams);

            // Set up the HTTP service
            HttpService service = new HttpService(
                httpProcessor,
                reuseStrategy,
                new DefaultHttpResponseFactory(),
                handlerRegistry,
                serverParams
            );

            // Start worker thread
            Thread t = new Thread(new Worker(service, connection));
            workers.add(t);
            t.setDaemon(true);
            t.start();

        }

        protected void cleanup() {
            workers.stream().filter(thread -> thread != null).forEach(Thread::interrupt);
        }

        /**
         * A worker for serving incoming requests.
         */
        protected class Worker implements Runnable {

            private final HttpService httpservice;
            private final HttpServerConnection conn;

            public Worker(HttpService httpservice, HttpServerConnection conn) {
                this.httpservice = httpservice;
                this.conn = conn;
            }

            public void run() {
                HttpContext context = new BasicHttpContext(null);
                try {
                    while ((servicedSocket != null) && this.conn.isOpen() && !Thread.interrupted()) {
                        this.httpservice.handleRequest(this.conn, context);
                    }
                } catch (IOException | HttpException ignore) {
                    // throttle ex;
                } finally {
                    workers.remove(Thread.currentThread());
                    try {
                        this.conn.shutdown();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }
}
