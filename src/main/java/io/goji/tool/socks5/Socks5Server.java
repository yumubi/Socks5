package io.goji.tool.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class Socks5Server implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Socks5Server.class);


    private final int port = 8888;
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;

    public Socks5Server() {
        try {
            this.selector = Selector.open();
            this.serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            LOGGER.info("Socks5 Server listening on port: {}", port);
            SelectionKey sk = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            sk.attach(new AcceptHandler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LinkedBlockingQueue<Socket> queue = new LinkedBlockingQueue<>();
        Thread.currentThread().join();

    }


    class AcceptHandler implements Runnable {
        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    new IOHandler(selector, socketChannel); // 注册IO处理器，并将连接加入select列表
                    new Socks5Handler(selector, socketChannel); // 注册Socks5处理器，并将连接加入select列表
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     *
     */
    @Override
    public void run() {

        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (var sk : selectionKeys) {
                    dispatch(sk);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void dispatch(SelectionKey selectedKey) {
        Runnable handler = (Runnable) selectedKey.attachment();
        // 此处返回的可能是AcceptHandler也可能是IOHandler
        handler.run();
    }

}

