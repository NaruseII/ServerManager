package fr.naruse.servermanager.proxy.velocity.packet;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import fr.naruse.servermanager.proxy.common.ProxyListeners;
import fr.naruse.servermanager.proxy.common.ProxyUtils;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.proxy.velocity.main.VelocityManagerPlugin;
import net.kyori.text.TextComponent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;

public class VelocityProcessPacketListener extends ProcessPacketListener {

    private final VelocityManagerPlugin pl;

    public VelocityProcessPacketListener(VelocityManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processReloadProxyServers(PacketReloadProxyServers packet) {
        Optional<RegisteredServer> optionalDefaultServer = this.pl.getProxyServer().getServer(packet.getDefaultServer());

        if(!optionalDefaultServer.isPresent() && !packet.getDefaultServer().equals("null")){
            Server server = ServerList.getByName(packet.getDefaultServer());

            if(server != null){
                optionalDefaultServer = Optional.of(this.buildServerInfo(server, packet.transformToLocalhostIfPossible()));
            }
        }

        this.pl.getProxyServer().getAllServers().stream().filter(registeredServer -> ServerList.getByName(registeredServer.getServerInfo().getName()) == null).forEach(registeredServer -> {
            this.pl.getProxyServer().unregisterServer(registeredServer.getServerInfo());
        });

        ServerList.getAll().stream().filter(s -> s.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER)).forEach(server -> {
            this.buildServerInfo(server, packet.transformToLocalhostIfPossible());
        });

        if(optionalDefaultServer.isPresent()){
            this.pl.getProxyServer().getConfiguration().getAttemptConnectionOrder().clear();
            this.pl.getProxyServer().getConfiguration().getAttemptConnectionOrder().add(optionalDefaultServer.get().getServerInfo().getName());
        }

        ServerManagerLogger.info("'config.yml' reloaded");
        if(optionalDefaultServer.isPresent()){
            ServerManagerLogger.info("Default server is '"+optionalDefaultServer.get().getServerInfo().getName()+"'");
        }else{
            ServerManagerLogger.info("Default server is 'null'");
        }
    }

    private RegisteredServer buildServerInfo(Server server, boolean transformToLocalhostIfPossible) {
        String address;
        try {
            if(transformToLocalhostIfPossible && InetAddress.getLocalHost().getHostAddress().equals(server.getAddress().getHostAddress())){
                address = "localhost:"+server.getPort();
            }else{
                address = server.getAddress().getHostAddress()+":"+server.getPort();
            }

            return this.pl.getProxyServer().registerServer(new ServerInfo(server.getName(), (InetSocketAddress) ProxyUtils.getAddress(address)));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
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
}
