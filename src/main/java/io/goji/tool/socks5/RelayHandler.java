package io.goji.tool.socks5;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class RelayHandler {


    public void doRelay(SocketChannel channel, String address, int port) {

        }
    }



    static class Pipe implements Runnable {
        private final SocketChannel from;
        private final SocketChannel to;
        private String id;


        public Pipe(SocketChannel from, SocketChannel to, String id) {
            this.from = from;
            this.to = to;
            this.id = id;
        }


        public void relay() {
            Thread t = new Thread(this);
            try {
                t.setName("Pipe-" + id + "-" + from.getRemoteAddress() + "-" + to.getRemoteAddress());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            t.start();
        }

        @Override
        public void run() {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (true) {
                    buffer.clear();
                    int len = from.read(buffer);
                    if (len > 0) {
                        buffer.flip();
                        to.write(buffer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


}
