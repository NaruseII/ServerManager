package fr.naruse.servermanager.core.connection.packet;

public class PacketProcessing {

    public void processAllPackets(IPacket packet) { }

    public void processReloadBungeeServers(PacketReloadBungeeServers packet){ }

    public void processBungeeRequestConfigWrite(PacketBungeeRequestConfigWrite packet){ }

    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) { }

    public void processSwitchServer(PacketSwitchServer packet) { }
}
