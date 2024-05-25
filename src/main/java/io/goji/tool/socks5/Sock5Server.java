package io.goji.tool.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Sock5Server implements Runnable{



    private static final Logger LOGGER = LoggerFactory.getLogger(Sock5Server.class);

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    private int connNum;
    private volatile long shutdownSignalTime = -1L;





    @Override
    public void run() {

    }


    private void init() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(null);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> {
                    LOGGER.info("Shutdown signal received");
                    shutdownSignalTime = System.nanoTime();
                    try {
                        Thread.sleep(Constants.SHUTDOWN_TIMEOUT_MILLIS);
                        selector.close();
                    } catch (IOException | InterruptedException e) {
                        LOGGER.error("Error while closing serverSocketChannel or selector", e);
                    } finally {
                        LOGGER.info("Shutdown complete");
                    }
                }));
        LOGGER.info("Server started on port {}", serverSocketChannel.socket().getLocalPort());
    }


    private void stop() {
        try {
            selector.close();
            serverSocketChannel.close();
            LOGGER.info("Server stopped");
        } catch (IOException e) {
            LOGGER.error("Error while closing serverSocketChannel or selector", e);
        }
    }


    private void eventLoop() throws IOException {
        boolean running = true;
        while (running) {
            if (shutdownSignalTime > 0) {
                if (System.nanoTime() - shutdownSignalTime > Constants.SHUTDOWN_TIMEOUT_MILLIS * 1_000_000L) {
                    running = false;
                }
            }

        }
    }

    private void HandlerEvent(boolean running) throws IOException {
        selector.select();
        Set<SelectionKey> sks = selector.selectedKeys();
        Iterator<SelectionKey> iterator = sks.iterator();
        while (iterator.hasNext()) {
            SelectionKey sk = iterator.next();
            iterator.remove();

            switch (sk.readyOps()) {
                case SelectionKey.OP_ACCEPT -> {
                    if(!running) {
                        continue;
                    }
                    // handle accept
                }
                case SelectionKey.OP_READ -> {
                    if(!running) {
                        continue;
                    }
                    // handle read
                }
                case SelectionKey.OP_WRITE -> {
                    // handle write
                }
                default -> {
                    LOGGER.error("Unexpected selection key: {}, occurred in Channel: {}", sk.readyOps(), sk.channel());
                    //close channel gracefully
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        //(HttpRequestHandler)key.attachment();


    }

    private void write(SelectionKey key) {
        //HttpRequestHandler handler = (HttpRequestHandler) key.attachment();

    }

    private void closeChannelSilently(SelectionKey key) {
        connNum--;
        try (SocketChannel channel = (SocketChannel) key.channel()) {
            key.cancel();
            LOGGER.info("Connection for Channel {} closed, current connections: {}", channel, connNum);
            //key.attachment();
            //...

        } catch (IOException e) {
            LOGGER.error("Error while closing channel", e);
        }

    }

}
