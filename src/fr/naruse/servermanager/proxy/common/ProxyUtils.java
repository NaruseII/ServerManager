package fr.naruse.servermanager.proxy.common;

import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.utils.Utils;
import io.netty.channel.unix.DomainSocketAddress;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyUtils {

    public static Configuration DEFAULT_SERVER_CONFIGURATION;
    public static Map<String, ProxyDefaultServer> PROXY_DEFAULT_SERVER_MAP = new HashMap<>();

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

    public static void load(File dataFolder){
        DEFAULT_SERVER_CONFIGURATION = new Configuration(new File(dataFolder, "proxyDefaultServers.json"), true);

        List<Map<String, Object>> list = DEFAULT_SERVER_CONFIGURATION.get("defaultServers");
        if(list == null){
            return;
        }
        for (Map<String, Object> map : list) {
            Configuration configuration = new Configuration(Utils.GSON.toJson(map));

            ProxyDefaultServer proxyDefaultServer = new ProxyDefaultServer(configuration.get("name"), configuration.get("address"), configuration.getInt("port"));
            if(proxyDefaultServer.getName().equals("Example Name That Will Not Be Used")){
                continue;
            }
            PROXY_DEFAULT_SERVER_MAP.put(proxyDefaultServer.getName(), proxyDefaultServer);
        }
    }

}
