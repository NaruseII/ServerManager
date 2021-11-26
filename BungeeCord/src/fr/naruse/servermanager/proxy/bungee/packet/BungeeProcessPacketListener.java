package fr.naruse.servermanager.proxy.bungee.packet;

import fr.naruse.api.config.Configuration;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.proxy.bungee.main.BungeeManagerPlugin;
import fr.naruse.servermanager.proxy.common.ProxyListeners;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeProcessPacketListener extends ProcessPacketListener {

    private final BungeeManagerPlugin pl;

    public BungeeProcessPacketListener(BungeeManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processSendTemplate(PacketSendTemplate packet) {
        this.pl.setTemplateConfiguration(new Configuration(packet.getJson()));
    }

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        BungeeCord.getInstance().getPluginManager().dispatchCommand(BungeeCord.getInstance().getConsole(), packet.getCommand());
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
