package fr.naruse.servermanager.bukkit.packet;

import fr.naruse.servermanager.bukkit.main.BukkitManagerPlugin;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.servermanager.core.connection.packet.PacketProcessing;
import org.bukkit.Bukkit;

public class BukkitPacketProcessing extends PacketProcessing {

    private final BukkitManagerPlugin pl;

    public BukkitPacketProcessing(BukkitManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), packet.getCommand());
    }
}
