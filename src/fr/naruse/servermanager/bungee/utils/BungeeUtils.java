package fr.naruse.servermanager.bungee.utils;

import io.netty.channel.unix.DomainSocketAddress;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class BungeeUtils {

    public static SocketAddress getAddr(String hostline) {
        URI uri = null;
        try
        {
            uri = new URI( hostline );
        } catch ( URISyntaxException ex )
        {
        }

        if ( uri != null && "unix".equals( uri.getScheme() ) )
        {
            return new DomainSocketAddress( uri.getPath() );
        }

        if ( uri == null || uri.getHost() == null )
        {
            try
            {
                uri = new URI( "tcp://" + hostline );
            } catch ( URISyntaxException ex )
            {
                throw new IllegalArgumentException( "Bad hostline: " + hostline, ex );
            }
        }

        if ( uri.getHost() == null )
        {
            throw new IllegalArgumentException( "Invalid host/address: " + hostline );
        }

        return new InetSocketAddress( uri.getHost(), ( uri.getPort() ) == -1 ? 25565 : uri.getPort() );
    }

}
