package fr.naruse.servermanager.packetmanager.packet;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.connection.packet.PacketDatabaseAnswer;
import fr.naruse.servermanager.core.connection.packet.PacketDatabaseRequest;
import fr.naruse.servermanager.core.connection.packet.PacketDatabaseRequestUpdate;
import fr.naruse.servermanager.core.connection.packet.ProcessPacketListener;
import fr.naruse.servermanager.core.database.Database;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.packetmanager.PacketManager;

public class PacketManagerProcessPacketListener extends ProcessPacketListener {

    private final PacketManager pl;

    public PacketManagerProcessPacketListener(PacketManager pl) {
        this.pl = pl;
    }

    @Override
    public void processDatabaseRequest(PacketDatabaseRequest packet) {
        if(pl.getServerManager().getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER) && packet.getServer() != null){
            switch (packet.getAction()) {
                case CLEAR:
                    pl.getDatabase().clear();
                    break;
                case REMOVE:
                    pl.getDatabase().remove(packet.getKey());
                    break;
                case PUT:
                    pl.getDatabase().put(packet.getKey(), packet.getDataObject());
                    break;
                case GET_ALL:
                    packet.getServer().sendPacket(new PacketDatabaseAnswer(packet.getCallbackId(), pl.getDatabase().getAll().toArray(new Database.DataObject[0])));
                    break;
                case GET:
                    packet.getServer().sendPacket(new PacketDatabaseAnswer(packet.getCallbackId(), new Database.DataObject[]{pl.getDatabase().get(packet.getKey())}));
                    break;
            }
            if(packet.needToSendUpdatePacket()){
                this.pl.getServerManager().getConnectionManager().sendPacketToAll(new PacketDatabaseRequestUpdate(packet));
            }
        }
    }
}
