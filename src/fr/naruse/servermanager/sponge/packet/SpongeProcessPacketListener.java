package fr.naruse.servermanager.sponge.packet;

import fr.naruse.servermanager.core.connection.packet.PacketBroadcast;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.servermanager.core.connection.packet.ProcessPacketListener;
import fr.naruse.servermanager.sponge.main.SpongeManagerPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

public class SpongeProcessPacketListener extends ProcessPacketListener {

    private SpongeManagerPlugin pl;

    public SpongeProcessPacketListener(SpongeManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), packet.getCommand());
    }

    @Override
    public void processBroadcast(PacketBroadcast packet) {
        Sponge.getServer().getBroadcastChannel().send(Text.of(packet.getMessage()));
    }
}
