package io.goji.tool.socks5;

public interface Constants {


    long SHUTDOWN_TIMEOUT_MILLIS = 10000L;

    int SOCKET_READ_DATA_LIMIT_BYTES = 32768;
    int SOCKET_READ_BUFFER_SIZE_BYTES = 8192;
    int FILE_READ_BUFFER_SIZE_BYTES = 8192;

    String SETTINGS_FILE_DEFAULT = "settings.properties";

    String SETTINGS_PORT = "port";
    String SETTINGS_MAX_CONNECTIONS = "max_connections";

    int SETTINGS_PORT_DEFAULT = 8080;

    int SETTINGS_MAX_CONNECTIONS_DEFAULT = 10000;

    final int ATYPE_IPv4 = 1;
    final int ATYPE_DOMAINNAME = 3;
    final int ATYPE_IPv6 = 4;


}
