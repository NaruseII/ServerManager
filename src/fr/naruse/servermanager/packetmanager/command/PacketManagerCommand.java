package fr.naruse.servermanager.packetmanager.command;

import com.diogonunes.jcolor.Attribute;
import fr.naruse.servermanager.core.command.AbstractCoreCommand;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.packetmanager.PacketManager;

public class PacketManagerCommand extends AbstractCoreCommand {

    private final PacketManager packetManager;

    public PacketManagerCommand(PacketManager packetManager) {
        super();
        this.packetManager = packetManager;
        this.registerCommand("stop", new CommandStop());
        this.registerCommand("generateSecretKey", new CommandGenerateSecretKey());
        this.registerCommand("status", new CommandStatus());
    }

    @Override
    public void help() {
        ServerManagerLogger.info(Attribute.CYAN_TEXT(), "Available commands:");
        ServerManagerLogger.info("");
        ServerManagerLogger.info("-> stop (Stop server)");
        ServerManagerLogger.info("-> generateSecretKey");
        ServerManagerLogger.info("-> status");
        for (String pluginCommandUsage : this.pluginCommandUsages) {
            ServerManagerLogger.info("-> "+pluginCommandUsage);
        }
        ServerManagerLogger.info("");
    }
}
