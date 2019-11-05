package net.cowpi.client.config;

import java.net.InetSocketAddress;

public interface ClientConfiguration {

    public InetSocketAddress getTcpServerAddress();

    public String getDbUrl();

}
