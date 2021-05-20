package fr.naruse.servermanager.bungee.packet;

import com.google.common.collect.Sets;
import fr.naruse.servermanager.bungee.main.BungeeManagerPlugin;
import fr.naruse.servermanager.core.connection.packet.PacketProcessing;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.ServerList;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class BungeePacketProcessing extends PacketProcessing {

    private final BungeeManagerPlugin pl;

    public BungeePacketProcessing(BungeeManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processReloadBungeeServers() {
        Sets.newHashSet(BungeeCord.getInstance().getServers().keySet()).stream().filter(s -> ServerList.getByName(s) == null).forEach(s -> BungeeCord.getInstance().getServers().remove(s));
        BungeeCord.getInstance().getConfig().load();

        System.out.println(ProxyServer.getInstance().getServerInfo( BungeeCord.getInstance().getConfig().getListeners().stream().findAny().get().getDefaultServer()));

        ServerManagerLogger.info("'config.yml' reloaded");
    }
}
