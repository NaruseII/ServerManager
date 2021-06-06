package fr.naruse.servermanager.core.connection.packet;

public class ProcessPacketListener {

    public void processAllPackets(IPacket packet) { }

    public void processReloadProxyServers(PacketReloadProxyServers packet){ }

    public void processProxyRequestConfigWrite(PacketProxyRequestConfigWrite packet){ }

    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) { }

    public void processSwitchServer(PacketSwitchServer packet) { }

    public void processBroadcast(PacketBroadcast packet) { }

    public void processDatabaseRequest(PacketDatabaseRequest packet) { }

    public void processDatabaseRequestUpdate(PacketDatabaseRequestUpdate packet) { }
}
