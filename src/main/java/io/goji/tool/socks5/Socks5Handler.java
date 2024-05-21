package io.goji.tool.socks5;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Socks5Handler {

    private final Selector selector;
    private final SocketChannel socketChannel;



    public Socks5Handler(Selector selector, SocketChannel socketChannel) {
        this.selector = selector;
        this.socketChannel = socketChannel;


    }

    //The client connects to the server, and sends a version identifier/method selection message:
//            |VER | NMETHODS | METHODS  |
//            +----+----------+----------+
//            | 1  |    1     | 1 to 255 |
//            +----+----------+----------+

    public void connect(SocketChannel socketChannel) {


        ByteBuffer buffer = ByteBuffer.allocate(257);

//        socketChannel.read(buffer);

        buffer.flip();


        byte[] bytes = new byte[buffer.remaining()];


    }



}
