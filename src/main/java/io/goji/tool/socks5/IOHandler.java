package io.goji.tool.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.SocketHandler;

public class IOHandler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(IOHandler.class);

    final private SocketChannel socketChannel;

    final private ByteBuffer buffer;


    public IOHandler(Selector selector, SocketChannel channel) {
        buffer = ByteBuffer.allocate(1024);
        socketChannel = channel;
        try {
            channel.configureBlocking(false);
            SelectionKey sk = channel.register(selector, 0); // 此处没有注册感兴趣的事件
            sk.attach(this);
            sk.interestOps(SelectionKey.OP_READ); // 注册感兴趣的事件，下一次调用select时才生效
            selector.wakeup(); // 立即唤醒当前阻塞select操作，使得迅速进入下次select，从而让上面注册的读事件监听可以立即生效
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            int length;

            while ((length = socketChannel.read(buffer)) > 0) {
                LOGGER.info("Read data length: {}\ndata: {}", length, new String(buffer.array(), 0, length));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
