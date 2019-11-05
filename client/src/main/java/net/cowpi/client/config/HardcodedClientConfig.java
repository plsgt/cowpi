package net.cowpi.client.config;

import java.net.InetSocketAddress;

public class HardcodedClientConfig implements ClientConfiguration {

    @Override
    public InetSocketAddress getTcpServerAddress() {
        return new InetSocketAddress("localhost", 9090);
    }

    @Override
    public String getDbUrl() {
        return "jdbc:sqlite:client.db";
    }
}
