package fr.naruse.servermanager.packetmanager.event;

import fr.naruse.servermanager.core.api.events.EventListener;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.core.connection.packet.PacketDatabase;
import fr.naruse.servermanager.core.database.DatabaseAPI;

import java.util.HashSet;

public class PacketManagerEventListener extends EventListener {

    @Override
    public void onServerRegisterEvent(ServerRegisterEvent e) {
        e.getServer().sendPacket(new PacketDatabase.UpdateCache(new HashSet<>(((DatabaseAPI.Cache) DatabaseAPI.getCache()).getTableMap().values())));
    }


}
