package io.goji.tool.socks5;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public abstract class HttpRequestHandler {

    public abstract void read(ReadableByteChannel channel) throws IOException;

    public abstract void write(WritableByteChannel channel) throws IOException;

    public abstract boolean hasNothingToWrite();

    public abstract void releaseSilently();

//    public static HttpRequestHandler build(ServerSettings settings, int connectionNum) {
//        return new HttpRequestHandlerImpl(settings, connectionNum);
//    }

}
