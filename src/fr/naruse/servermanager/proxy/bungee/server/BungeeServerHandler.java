package fr.naruse.servermanager.proxy.bungee.server;

import com.google.common.collect.Sets;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.proxy.bungee.main.BungeeManagerPlugin;
import fr.naruse.servermanager.proxy.common.ProxyDefaultServer;
import fr.naruse.servermanager.proxy.common.ProxyListeners;
import fr.naruse.servermanager.proxy.common.ProxyUtils;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Optional;
import java.util.Set;

public class BungeeServerHandler {

    public static void reloadServers(BungeeManagerPlugin pl){
        reloadServers(pl, true);
    }

    public static void reloadServers(BungeeManagerPlugin pl, boolean log){
        if(pl.getTemplateConfiguration() == null){
            return;
        }

        Optional<Server> optionalDefaultServer = ProxyListeners.findDefaultServer(pl.getTemplateConfiguration());
        ServerInfo defaultServer = pl.getProxy().getServerInfo(pl.getListenerInfo().getDefaultServer());

        // Adding default server
        if(optionalDefaultServer.isPresent()){
            Server server = optionalDefaultServer.get();
            ServerInfo newServerInfo = pl.getProxy().getServerInfo(server.getName());
            if(newServerInfo == null){
                defaultServer = buildServerInfo(server.getName(), server.getAddress().getHostAddress(), server.getPort(), false);

                BungeeCord.getInstance().getServers().put(server.getName(), defaultServer);
            }else{
                defaultServer = newServerInfo;
            }
        }


        // Removing un found servers
        Set<String> set = Sets.newHashSet(BungeeCord.getInstance().getServers().keySet());
        ServerInfo finalDefaultServer = defaultServer;

        set.stream().filter(s -> ServerList.getByName(s) == null && !ProxyUtils.PROXY_DEFAULT_SERVER_MAP.containsKey(s)).forEach(s -> {
            ServerInfo serverInfo = BungeeCord.getInstance().getServerInfo(s);

            if(finalDefaultServer != null && serverInfo != null){
                for (ProxiedPlayer p : serverInfo.getPlayers()) {
                    p.connect(finalDefaultServer);
                }
            }

            BungeeCord.getInstance().getServers().remove(s);
        });


        // Adding new servers
        ServerList.getAll().stream().filter(s -> !set.contains(s.getName()) && s.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER, CoreServerType.NUKKIT_MANAGER)).forEach(server -> {
            ServerInfo serverInfo = buildServerInfo(server.getName(), server.getAddress().getHostAddress(), server.getPort(), false);
            BungeeCord.getInstance().getServers().put(server.getName(), serverInfo);
        });
        for (ProxyDefaultServer server : ProxyUtils.PROXY_DEFAULT_SERVER_MAP.values()) {
            ServerInfo serverInfo = buildServerInfo(server.getName(), server.hostAddress(), server.getPort(), false);
            BungeeCord.getInstance().getServers().put(server.getName(), serverInfo);
        }


        // Setting default server
        if(defaultServer != null){
            BungeeCord.getInstance().getConfig().getListeners().stream().forEach(listenerInfo -> {
                listenerInfo.getServerPriority().clear();
                listenerInfo.getServerPriority().add(finalDefaultServer.getName());
                pl.setListenerInfo(listenerInfo);
            });
        }

       if(log){
           if(defaultServer != null){
               ServerManagerLogger.info("Default server is '"+defaultServer.getName()+"'");
           }else{
               ServerManagerLogger.info("Default server is 'null'");
           }
       }
    }

    private static ServerInfo buildServerInfo(String name, String hostAddress, int port, boolean transformToLocalhostIfPossible){
        String address;
        if(transformToLocalhostIfPossible && Utils.getLocalHost().getHostAddress().equals(hostAddress)){
            address = "localhost:"+port;
        }else{
            address = hostAddress+":"+port;
        }

        return ProxyServer.getInstance().constructServerInfo(name, ProxyUtils.getAddress(address), name, false);
    }

    public static void clear(BungeeManagerPlugin pl) {
        pl.getProxy().getServers().clear();
    }
}
