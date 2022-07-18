package fr.naruse.servermanager.filemanager.packet;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.command.AbstractCoreCommand;
import fr.naruse.servermanager.core.connection.packet.PacketCreateTemplate;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.servermanager.core.connection.packet.PacketSaverServer;
import fr.naruse.servermanager.core.connection.packet.ProcessPacketListener;
import fr.naruse.servermanager.filemanager.FileManager;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.io.File;

public class FileManagerPacketListener extends ProcessPacketListener {

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        AbstractCoreCommand.get().execute(packet.getCommand());
    }

    @Override
    public void processCreateTemplate(PacketCreateTemplate packet) {
        File file = new File(ServerManager.get().getConfigurationManager().getServerTemplateFolder(), packet.getFileName());
        packet.getTemplateConfiguration().save(file);
        ServerManager.get().getConfigurationManager().loadTemplates();
    }

    @Override
    public void processSaveServer(PacketSaverServer packet) {
        ServerProcess serverProcess = FileManager.get().getServerProcess(packet.getServerName());
        if(serverProcess != null){
            serverProcess.setSaveOnShutdown(packet.shouldSaveOnShutdown(), packet.getSaveKey());
        }
    }
}
