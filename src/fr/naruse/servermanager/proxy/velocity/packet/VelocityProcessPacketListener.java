package fr.naruse.servermanager.proxy.velocity.packet;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import fr.naruse.servermanager.core.config.Configuration;
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
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.util.Optional;

public class VelocityProcessPacketListener extends ProcessPacketListener {

    private final VelocityManagerPlugin pl;

    public VelocityProcessPacketListener(VelocityManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processSendTemplate(PacketSendTemplate packet) {
        this.pl.setTemplateConfiguration(new Configuration(packet.getJson()));
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
        for (Player allPlayer : this.pl.getProxyServer().getAllPlayers()) {
            allPlayer.sendMessage(Component.text(packet.getMessage()));
        }
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
            optional.get().disconnect(Component.text(packet.getReason() == null ? "" : packet.getReason()));
        }
    }
}
