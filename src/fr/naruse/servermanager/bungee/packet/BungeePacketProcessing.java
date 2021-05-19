package fr.naruse.servermanager.bungee.packet;

import fr.naruse.servermanager.bungee.main.BungeeManagerPlugin;
import fr.naruse.servermanager.core.connection.packet.PacketProcessing;
import net.md_5.bungee.BungeeCord;

public class BungeePacketProcessing extends PacketProcessing {

    private final BungeeManagerPlugin pl;

    public BungeePacketProcessing(BungeeManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processReloadBungeeServers() {
        BungeeCord.getInstance().getConfig().load();
    }
}
