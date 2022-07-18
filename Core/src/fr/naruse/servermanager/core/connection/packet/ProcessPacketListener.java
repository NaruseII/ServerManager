package fr.naruse.servermanager.core.connection.packet;

public class ProcessPacketListener {

    public void processAllPackets(IPacket packet) { }

    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) { }

    public void processSwitchServer(PacketSwitchServer packet) { }

    public void processBroadcast(PacketBroadcast packet) { }

    public void processTeleportToLocation(PacketTeleportToLocation packet) { }

    public void processTeleportToPlayer(PacketTeleportToPlayer packet) { }

    public void processKickPlayer(PacketKickPlayer packet) { }

    public void processSendTemplate(PacketSendTemplate packet) { }

    public void processCreateTemplate(PacketCreateTemplate packet) { }

    public void processSaveServer(PacketSaverServer packet){ }
}
