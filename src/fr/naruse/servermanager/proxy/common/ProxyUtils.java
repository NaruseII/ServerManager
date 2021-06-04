package fr.naruse.servermanager.proxy.common;

import io.netty.channel.unix.DomainSocketAddress;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class ProxyUtils {

    public static SocketAddress getAddress(String host) {
        URI uri = null;
        try {
            uri = new URI( host );
        } catch ( URISyntaxException ex ) { }

        if ( uri != null && "unix".equals( uri.getScheme() ) ) {
            return new DomainSocketAddress( uri.getPath() );
        }

        if ( uri == null || uri.getHost() == null ) {
            try {
                uri = new URI( "tcp://" + host );
            } catch ( URISyntaxException ex ) {
                throw new IllegalArgumentException( "Bad hostline: " + host, ex );
            }
        }

        if ( uri.getHost() == null ) {
            throw new IllegalArgumentException( "Invalid host/address: " + host );
        }

        return new InetSocketAddress( uri.getHost(), ( uri.getPort() ) == -1 ? 25565 : uri.getPort() );
    }

}
