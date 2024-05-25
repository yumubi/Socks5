package io.goji.tool.socks5;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public class Socks5handler extends HttpRequestHandler {


    /*
     *  request:
     *  +----+----------+----------+
     *  |VER | NMETHODS | METHODS  |
     *  +----+----------+----------+
     *  | 1  |    1     | 1 to 255 |
     *  +----+----------+----------+
     *
     *   response:
     *   +----+--------+
     *   |VER | METHOD |
     *   +----+--------+
     *   | 1  |   1    |
     *   +----+--------+
     */
    private void connect(SocketChannel channel, boolean allowNoAuth) throws IOException {

        //        -------segment 1------
        byte[] buffer = new byte[257];
        int len = channel.read(ByteBuffer.wrap(buffer));
        if(len <= 0) {
            channel.close();
            return;
        }

        // read VER
        int ver = buffer[0];
        if(ver != 0x05) {
            channel.write(ByteBuffer.wrap(new byte[]{0x05, (byte) 0xFF}));
            return;
        }
        //        -------segment 1------


        //        -------segment 2------
        //NO AUTHENTICATION REQUIRED
        if(allowNoAuth) {
            channel.write(ByteBuffer.wrap(new byte[]{0x05, (byte) 0xFF}));
            //waitingRequest(channel);
            return;
        }

        if(len <= 1) {
            channel.write(ByteBuffer.wrap(new byte[]{0x05, (byte) 0xFF}));
            return;
        }
        //        -------segment 2------


        //[RFC1929](https://datatracker.ietf.org/doc/html/rfc1929)中定义了一种认证方式，即用户名密码认证
        //        -------segment 3------
        //METHODS
        int nMethods = buffer[1];
        for(int i = 0; i < nMethods; i++) {
            //username and password authentication
            if(buffer[i + 2] == 0x02) {
                channel.write(ByteBuffer.wrap(new byte[]{0x05, (byte) 0x02}));
                if(doAuth(channel)) {
                    //waitingRequest(channel);
                }
                return;
            }
        }

        channel.write(ByteBuffer.wrap(new byte[]{0x05, (byte) 0xFF}));

    }


    /**
     *  +----+------+----------+------+----------+
     *  |VER | ULEN |  UNAME   | PLEN |  PASSWD  |
     *  +----+------+----------+------+----------+
     *  | 1  |  1   | 1 to 255 |  1   | 1 to 255 |
     *  +----+------+----------+------+----------+
     *
     *  +----+--------+
     *  |VER | STATUS |
     *  +----+--------+
     *  | 1  |   1    |
     *  +----+--------+
     */
    private static boolean doAuth(SocketChannel channel) throws IOException {
        byte[] buffer = new byte[512];
        int len = channel.read(ByteBuffer.wrap(buffer));
        if(len <= 0) {
            channel.close();
            return false;
        }

        // read VER
        int ver = buffer[0];
        if(ver != 0x01) {
            channel.write(ByteBuffer.wrap(new byte[]{0x05, (byte) 0x01}));
            return false;
        }

        if(len <= 1) {
            channel.write(ByteBuffer.wrap(new byte[]{0x05, (byte) 0x01}));
            return false;
        }


        UserInfo userInfo = UserInfo.of(buffer);
        if(userInfo.match("admin", "admin")) {
            channel.write(ByteBuffer.wrap(new byte[]{0x01, (byte) 0x00}));
            return true;
        }
        //        0x01: general SOCKS server failure
        channel.write(ByteBuffer.wrap(new byte[]{0x01, (byte) 0x01}));
        return false;
    }


    /**
     *   socks5 client request
     *   +----+-----+-------+------+----------+----------+
     *   |VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
     *   +----+-----+-------+------+----------+----------+
     *   | 1  |  1  | X'00' |  1   | Variable |    2     |
     *   +----+-----+-------+------+----------+----------+
     *
     *   socks5 server response
     *   +----+-----+-------+------+----------+----------+
     *   |VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
     *   +----+-----+-------+------+----------+----------+
     *   | 1  |  1  | X'00' |  1   | Variable |    2     |
     *   +----+-----+-------+------+----------+----------+
     */

    private void waitRequest(SocketChannel channel) throws IOException {
        byte[] buffer = new byte[256];

        int len = channel.read(ByteBuffer.wrap(buffer));
        if(len <= 0) {
            channel.close();
            return;
        }
        int ver = buffer[0];
        if(ver != 0x05) {
            // socks version error, close the connection,
            // 0x05: general SOCKS server failure,
            // 0x00: succeeded,
            // 0x01: general SOCKS server failure,
            // 0x02: connection not allowed by ruleset,
            // 0x03: Network unreachable, 0x04: Host unreachable,
            // 0x05: Connection refused,
            // 0x06: TTL expired, 0x07: Command not supported,
            // 0x08: Address type not supported, 0x09: to X'FF' unassigned,
            // 0x10: to X'FF' unassigned, 0x11: to X'FF' unassigned,
            // 0x12: to X'FF' unassigned, 0x13: to X'FF' unassigned, 0x14: to X'FF' unassigned,
            // 0x15: to X'FF' unassigned, 0x16: to X'FF' unassigned, 0x17: to X'FF' unassigned,
            // 0x18: to X'FF' unassigned, 0x19: to X'FF' unassigned, 0x1A: to X'FF' unassigned,
            // 0x1B: to X'FF' unassigned, 0x1C: to X'FF' unassigned, 0x1D: to X'FF' unassigned,
            // 0x1E: to X'FF' unassigned, 0x1F: to X'FF' unassigned
            channel.write(ByteBuffer.wrap(new byte[]{5, 1, 0, 1, 0, 0, 0, 0, 0, 0}));
            return;
        }
        int cmd = buffer[1];
        if(cmd != 0x01) {
            // 0x01: CONNECT, 0x02: BIND, 0x03: UDP ASSOCIATE
            channel.write(ByteBuffer.wrap(new byte[]{5, 1, 0, 1, 0, 0, 0, 0, 0, 0}));
            return;
        }

        RemoteAddr remoteAddress = getRemoteAddr(buffer, len);
        channel.write(ByteBuffer.wrap(new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0}));

        //relay(channel, remoteAddress);
    }

    private RemoteAddr getRemoteAddr(byte[] bytes, int len) {
        byte atyp = bytes[3];
        String addr;

        try {
            if(atyp == Constants.ATYPE_IPv4) {
                byte[] ipv4 = new byte[4];
                System.arraycopy(bytes, 4, ipv4, 0, 4);
                addr = Inet4Address.getByAddress(ipv4).getHostAddress();
            } else if(atyp == Constants.ATYPE_IPv6) {
                byte[] ipv6 = new byte[16];
                System.arraycopy(bytes, 4, ipv6, 0, 16);
                addr = Inet6Address.getByAddress(ipv6).getHostAddress();
            } else if(atyp == Constants.ATYPE_DOMAINNAME) {
                int domainLen = bytes[4];
                byte[] domainBytes = new byte[domainLen];
                System.arraycopy(bytes, 5, domainBytes, 0, domainLen);
                addr = new String(domainBytes);
            }else {
                throw new RuntimeException("Unknown address type: " + atyp);
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        RemoteAddr remoteAddr = new RemoteAddr();
        remoteAddr.addr = addr.trim();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{bytes[len - 2], bytes[len - 1]});
        remoteAddr.port = buffer.getShort();
        return remoteAddr;

    }




    /**
     * @param channel
     * @throws IOException
     */
    @Override
    public void read(ReadableByteChannel channel) throws IOException {

    }

    /**
     * @param channel
     * @throws IOException
     */
    @Override
    public void write(WritableByteChannel channel) throws IOException {

    }

    /**
     * @return
     */
    @Override
    public boolean hasNothingToWrite() {
        return false;
    }

    /**
     *
     */
    @Override
    public void releaseSilently() {

    }


    private static class UserInfo {
        String username;
        String password;

        public static UserInfo of(byte[] data) {
            int uLen = data[1];
            byte[] uBytes = new byte[uLen];
            System.arraycopy(data, 2, uBytes, 0, uLen);
            UserInfo userInfo = new UserInfo();
            userInfo.username = new String(uBytes);
            int pLen = data[2 + uLen];
            byte[] pBytes = new byte[pLen];
            System.arraycopy(data, 3 + uLen, pBytes, 0, pLen);
            userInfo.password = new String(pBytes);
            return userInfo;
        }


        public boolean match(String username,String password) {
            return username.equals(this.username) && password.equals(this.password);
        }
    }


    private static class RemoteAddr {
        public String addr;
        public int port;

        @Override
        public String toString() {
            return "RemoteAddr{" +
                    "addr='" + addr + '\'' +
                    ", port=" + port +
                    '}';
        }
    }
}
