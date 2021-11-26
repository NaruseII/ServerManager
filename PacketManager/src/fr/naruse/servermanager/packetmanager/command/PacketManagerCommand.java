package fr.naruse.servermanager.packetmanager.command;

import com.diogonunes.jcolor.Attribute;
import fr.naruse.servermanager.core.command.AbstractCoreCommand;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.packetmanager.PacketManager;

public class PacketManagerCommand extends AbstractCoreCommand {

    private final PacketManager packetManager;

    public PacketManagerCommand(PacketManager packetManager) {
        super();
        this.packetManager = packetManager;
        this.registerCommand("stop", new CommandStop());
        this.registerCommand("generateSecretKey", new CommandGenerateSecretKey());
        this.registerCommand("status", new CommandStatus());
        this.registerCommand("emptyDatabases", new CommandEmptyDatabases());
    }

    @Override
    public void help() {
        GlobalLogger.info(Attribute.CYAN_TEXT(), "Available commands:");
        GlobalLogger.info("");
        GlobalLogger.info("-> stop (Stop server)");
        GlobalLogger.info("-> generateSecretKey");
        GlobalLogger.info("-> status");
        GlobalLogger.info("-> emptyDatabases");
        for (String pluginCommandUsage : this.pluginCommandUsages) {
            GlobalLogger.info("-> "+pluginCommandUsage);
        }
        GlobalLogger.info("");
    }

    private class CommandEmptyDatabases implements ICommand {
        @Override
        public void onCommand(String line, String[] args) {
            packetManager.getDatabase().destroyAll();
            GlobalLogger.info("Done");
        }
    }
}
