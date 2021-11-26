package fr.naruse.servermanager.proxy.velocity.server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.proxy.common.ProxyDefaultServer;
import fr.naruse.servermanager.proxy.common.ProxyListeners;
import fr.naruse.servermanager.proxy.common.ProxyUtils;
import fr.naruse.servermanager.proxy.velocity.main.VelocityManagerPlugin;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

public class VelocityServerHandler {

    public static void reloadServers(VelocityManagerPlugin pl) {
        reloadServers(pl, true);
    }

    public static void reloadServers(VelocityManagerPlugin pl, boolean log) {
        if(pl.getTemplateConfiguration() == null){
            return;
        }

        Optional<Server> optionalNewDefaultServer = ProxyListeners.findDefaultServer(pl.getTemplateConfiguration());

        List<String> serverOrderList = pl.getProxyServer().getConfiguration().getAttemptConnectionOrder();
        Optional<RegisteredServer> defaultServer = serverOrderList.isEmpty() ? null : pl.getProxyServer().getServer(serverOrderList.get(0));
        // Adding default server
        if(optionalNewDefaultServer.isPresent()){
            Server server = optionalNewDefaultServer.get();
            Optional<RegisteredServer> newServerInfo = pl.getProxyServer().getServer(server.getName());
            if(!newServerInfo.isPresent()){
                defaultServer = Optional.of(buildServerInfo(pl, server.getName(), server.getAddress().getHostAddress(), server.getPort(), false));
            }else{
                defaultServer = newServerInfo;
            }
        }

        // Removing un found servers
        pl.getProxyServer().getAllServers().stream().filter(registeredServer -> {
            String name = registeredServer.getServerInfo().getName();
            return ServerList.getByName(name) == null && !ProxyUtils.PROXY_DEFAULT_SERVER_MAP.containsKey(name);
        }).forEach(registeredServer -> {
            pl.getProxyServer().unregisterServer(registeredServer.getServerInfo());
        });

        // Adding new servers
        ServerList.getAll().stream().filter(s -> s.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER, CoreServerType.NUKKIT_MANAGER)).forEach(server -> {
            buildServerInfo(pl, server.getName(), server.getAddress().getHostAddress(), server.getPort(), false);
        });
        for (ProxyDefaultServer server : ProxyUtils.PROXY_DEFAULT_SERVER_MAP.values()) {
            buildServerInfo(pl, server.getName(), server.hostAddress(), server.getPort(), false);
        }

        // Setting default server
        if(defaultServer.isPresent()){
            pl.getProxyServer().getConfiguration().getAttemptConnectionOrder().clear();
            pl.getProxyServer().getConfiguration().getAttemptConnectionOrder().add(defaultServer.get().getServerInfo().getName());
        }

        if(log){
            if(defaultServer.isPresent()){
                GlobalLogger.info("Default server is '"+defaultServer.get().getServerInfo().getName()+"'");
            }else{
                GlobalLogger.info("Default server is 'null'");
            }
        }
    }

    private static RegisteredServer buildServerInfo(VelocityManagerPlugin pl, String name, String hostAddress, int port, boolean transformToLocalhostIfPossible) {
        String address;
        if(transformToLocalhostIfPossible && Utils.getLocalHost().getHostAddress().equals(hostAddress)){
            address = "localhost:"+port;
        }else{
            address = hostAddress+":"+port;
        }

        return pl.getProxyServer().registerServer(new ServerInfo(name, (InetSocketAddress) ProxyUtils.getAddress(address)));
    }


}
