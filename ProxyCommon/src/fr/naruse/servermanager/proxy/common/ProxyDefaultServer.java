package fr.naruse.servermanager.proxy.common;

public class ProxyDefaultServer {

    private final String name;
    private final String address;
    private final int port;

    public ProxyDefaultServer(String name, String address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String hostAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
