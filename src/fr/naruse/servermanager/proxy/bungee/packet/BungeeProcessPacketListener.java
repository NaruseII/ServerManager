package fr.naruse.servermanager.proxy.bungee.packet;

import com.google.common.collect.Sets;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.proxy.bungee.main.BungeeManagerPlugin;
import fr.naruse.servermanager.proxy.common.ProxyDefaultServer;
import fr.naruse.servermanager.proxy.common.ProxyListeners;
import fr.naruse.servermanager.proxy.common.ProxyUtils;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Set;

public class BungeeProcessPacketListener extends ProcessPacketListener {

    private final BungeeManagerPlugin pl;

    public BungeeProcessPacketListener(BungeeManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processReloadProxyServers(PacketReloadProxyServers packet) {
        ServerInfo defaultServer = BungeeCord.getInstance().getServerInfo(packet.getDefaultServer());

        // Adding default server
        if(defaultServer == null && !packet.getDefaultServer().equals("null")){
            Server server = ServerList.getByName(packet.getDefaultServer());
            if(server != null){
                defaultServer = this.buildServerInfo(server.getName(), server.getAddress().getHostAddress(), server.getPort(), packet.transformToLocalhostIfPossible());

                BungeeCord.getInstance().getServers().put(server.getName(), defaultServer);
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
        ServerList.getAll().stream().filter(s -> !set.contains(s.getName()) && s.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER)).forEach(server -> {
            ServerInfo serverInfo = this.buildServerInfo(server.getName(), server.getAddress().getHostAddress(), server.getPort(), packet.transformToLocalhostIfPossible());
            BungeeCord.getInstance().getServers().put(server.getName(), serverInfo);
        });
        for (ProxyDefaultServer server : ProxyUtils.PROXY_DEFAULT_SERVER_MAP.values()) {
            ServerInfo serverInfo = this.buildServerInfo(server.getName(), server.hostAddress(), server.getPort(), packet.transformToLocalhostIfPossible());
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

        ServerManagerLogger.info("Servers reloaded");
        if(defaultServer != null){
            ServerManagerLogger.info("Default server is '"+defaultServer.getName()+"'");
        }else{
            ServerManagerLogger.info("Default server is 'null'");
        }
    }

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        BungeeCord.getInstance().getPluginManager().dispatchCommand(BungeeCord.getInstance().getConsole(), packet.getCommand());
    }

    private ServerInfo buildServerInfo(String name, String hostAddress, int port, boolean transformToLocalhostIfPossible){
        String address;
        if(transformToLocalhostIfPossible && Utils.getLocalHost().getHostAddress().equals(hostAddress)){
            address = "localhost:"+port;
        }else{
            address = hostAddress+":"+port;
        }

        return ProxyServer.getInstance().constructServerInfo(name, ProxyUtils.getAddress(address), name, false);
    }


    @Override
    public void processSwitchServer(PacketSwitchServer packet) {
        if(packet.getServer() == null){
            return;
        }

        ServerInfo serverInfo = BungeeCord.getInstance().getServerInfo(packet.getServer().getName());
        if(serverInfo == null){
            for (ServerInfo value : BungeeCord.getInstance().getServers().values()) {
                if (value.getAddress().getPort() == packet.getServer().getPort()) {
                    serverInfo = value;
                    break;
                }
            }
        }

        if(serverInfo == null){
            return;
        }

        for (String name : packet.getNames()) {
            ProxiedPlayer player = BungeeCord.getInstance().getPlayer(name);
            if(player != null && !player.getServer().getInfo().equals(serverInfo)){
                player.connect(serverInfo);
            }
        }
    }

    @Override
    public void processBroadcast(PacketBroadcast packet) {
        BungeeCord.getInstance().broadcast(packet.getMessage());
    }

    @Override
    public void processTeleportToLocation(PacketTeleportToLocation packet) {
        ProxyListeners.processTeleportToLocation(packet);
    }

    @Override
    public void processTeleportToPlayer(PacketTeleportToPlayer packet) {
        ProxyListeners.processTeleportToPlayer(this, packet);
    }

    @Override
    public void processKickPlayer(PacketKickPlayer packet) {
        ProxiedPlayer player = BungeeCord.getInstance().getPlayer(packet.getPlayerName());
        if(player != null){
            player.disconnect(packet.getReason() == null ? "" : packet.getReason());
        }
    }
}
