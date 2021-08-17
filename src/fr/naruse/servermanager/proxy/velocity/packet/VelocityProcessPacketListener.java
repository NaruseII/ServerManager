package fr.naruse.servermanager.proxy.velocity.packet;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.proxy.common.ProxyDefaultServer;
import fr.naruse.servermanager.proxy.common.ProxyListeners;
import fr.naruse.servermanager.proxy.common.ProxyUtils;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.proxy.velocity.main.VelocityManagerPlugin;
import net.kyori.text.TextComponent;

import java.net.InetSocketAddress;
import java.util.Optional;

public class VelocityProcessPacketListener extends ProcessPacketListener {

    private final VelocityManagerPlugin pl;

    public VelocityProcessPacketListener(VelocityManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processReloadProxyServers(PacketReloadProxyServers packet) {
        Optional<RegisteredServer> optionalDefaultServer = this.pl.getProxyServer().getServer(packet.getDefaultServer());

        // Adding default server
        if(!optionalDefaultServer.isPresent() && !packet.getDefaultServer().equals("null")){
            Server server = ServerList.getByName(packet.getDefaultServer());

            if(server != null){
                optionalDefaultServer = Optional.of(this.buildServerInfo(server.getName(), server.getAddress().getHostAddress(), server.getPort(), packet.transformToLocalhostIfPossible()));
            }
        }

        // Removing un found servers
        this.pl.getProxyServer().getAllServers().stream().filter(registeredServer -> {
            String name = registeredServer.getServerInfo().getName();
            return ServerList.getByName(name) == null && !ProxyUtils.PROXY_DEFAULT_SERVER_MAP.containsKey(name);
        }).forEach(registeredServer -> {
            this.pl.getProxyServer().unregisterServer(registeredServer.getServerInfo());
        });

        // Adding new servers
        ServerList.getAll().stream().filter(s -> s.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER)).forEach(server -> {
            this.buildServerInfo(server.getName(), server.getAddress().getHostAddress(), server.getPort(), packet.transformToLocalhostIfPossible());
        });
        for (ProxyDefaultServer server : ProxyUtils.PROXY_DEFAULT_SERVER_MAP.values()) {
            this.buildServerInfo(server.getName(), server.hostAddress(), server.getPort(), packet.transformToLocalhostIfPossible());
        }

        // Setting default server
        if(optionalDefaultServer.isPresent()){
            this.pl.getProxyServer().getConfiguration().getAttemptConnectionOrder().clear();
            this.pl.getProxyServer().getConfiguration().getAttemptConnectionOrder().add(optionalDefaultServer.get().getServerInfo().getName());
        }

        ServerManagerLogger.info("Servers reloaded");
        if(optionalDefaultServer.isPresent()){
            ServerManagerLogger.info("Default server is '"+optionalDefaultServer.get().getServerInfo().getName()+"'");
        }else{
            ServerManagerLogger.info("Default server is 'null'");
        }
    }

    private RegisteredServer buildServerInfo(String name, String hostAddress, int port, boolean transformToLocalhostIfPossible) {
        String address;
        if(transformToLocalhostIfPossible && Utils.getLocalHost().getHostAddress().equals(hostAddress)){
            address = "localhost:"+port;
        }else{
            address = hostAddress+":"+port;
        }

        return this.pl.getProxyServer().registerServer(new ServerInfo(name, (InetSocketAddress) ProxyUtils.getAddress(address)));
    }

    @Override
    public void processSwitchServer(PacketSwitchServer packet) {
        if(packet.getServer() == null) {
            return;
        }

        Optional<RegisteredServer> optionalServer = this.pl.getProxyServer().getServer(packet.getServer().getName());
        if(!optionalServer.isPresent()){
            for (RegisteredServer server : this.pl.getProxyServer().getAllServers()) {
                if(server.getServerInfo().getAddress().getPort() == packet.getServer().getPort()){
                    optionalServer = Optional.of(server);
                    break;
                }
            }
        }

        if(!optionalServer.isPresent()){
            return;
        }

        for (String name : packet.getNames()) {
            Optional<Player> optionalPlayer = this.pl.getProxyServer().getPlayer(name);

            if(optionalPlayer.isPresent()){
                Player player = optionalPlayer.get();
                if(player != null && (player.getCurrentServer().isPresent() && !player.getCurrentServer().get().equals(optionalServer.get()))){
                    player.createConnectionRequest(optionalServer.get()).connect();
                }
            }
        }
    }

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        this.pl.getProxyServer().getCommandManager().executeAsync(this.pl.getProxyServer().getConsoleCommandSource(), packet.getCommand());
    }

    @Override
    public void processBroadcast(PacketBroadcast packet) {
        this.pl.getProxyServer().broadcast(TextComponent.of(packet.getMessage()));
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
        Optional<Player> optional = pl.getProxyServer().getPlayer(packet.getPlayerName());
        if(optional.isPresent()){
            optional.get().disconnect(TextComponent.of(packet.getReason() == null ? "" : packet.getReason()));
        }
    }
}
