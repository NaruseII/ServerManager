package fr.naruse.servermanager.bukkit.packet;

import fr.naruse.servermanager.bukkit.main.BukkitManagerPlugin;
import fr.naruse.servermanager.core.connection.packet.PacketBroadcast;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.servermanager.core.connection.packet.ProcessPacketListener;
import org.bukkit.Bukkit;

public class BukkitProcessPacketListener extends ProcessPacketListener {

    private final BukkitManagerPlugin pl;

    public BukkitProcessPacketListener(BukkitManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), packet.getCommand());
    }

    @Override
    public void processBroadcast(PacketBroadcast packet) {
        Bukkit.broadcastMessage(packet.getMessage());
    }
}
