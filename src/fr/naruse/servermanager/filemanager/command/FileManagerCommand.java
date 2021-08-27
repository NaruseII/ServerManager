package fr.naruse.servermanager.filemanager.command;

import com.diogonunes.jcolor.Attribute;
import fr.naruse.servermanager.core.command.AbstractCoreCommand;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.filemanager.FileManager;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.io.File;
import java.util.HashSet;

public class FileManagerCommand extends AbstractCoreCommand {

    private final FileManager fileManager;

    public FileManagerCommand(FileManager fileManager) {
        this.fileManager = fileManager;
        this.registerCommand("stop", new CommandStop());
        this.registerCommand("shutdown", new CommandShutdown());
        this.registerCommand("status", new CommandStatus());
        this.registerCommand("generateSecretKey", new CommandGenerateSecretKey());
        this.registerCommand("create", new CommandCreate());
        this.registerCommand("scale", new CommandScale());
        this.registerCommand("insertCommand", new CommandInsertCommand());
        this.registerCommand("deleteUnUsedLogs", new CommandDeleteUsedLogs());
        this.registerCommand("screen", new CommandScreenList());
        this.registerCommand("viewScreen", new CommandViewScreen());
        this.registerCommand("detach", new CommandDetach());
        this.registerCommand("reloadTemplates", new CommandReloadTemplates());
    }

    @Override
    public void help() {
        ServerManagerLogger.info(Attribute.CYAN_TEXT(), "Available commands:");
        ServerManagerLogger.info("");
        ServerManagerLogger.info("-> stop (Stop server)");
        ServerManagerLogger.info("-> shutdown <Server name, All>");
        ServerManagerLogger.info("-> status");
        ServerManagerLogger.info("-> generateSecretKey");
        ServerManagerLogger.info("-> create <Template Name> <[count]>");
        ServerManagerLogger.info("-> scale");
        ServerManagerLogger.info("-> insertCommand <Server name> <Cmd>");
        ServerManagerLogger.info("-> deleteUnUsedLogs");
        ServerManagerLogger.info("-> screen -l (List all processes)");
        ServerManagerLogger.info("-> viewScreen <Process Name> (Attach on screen)");
        ServerManagerLogger.info("-> detach (Detach from current screen)");
        ServerManagerLogger.info("-> reloadTemplates");
        ServerManagerLogger.info("");
    }

    private class CommandShutdown implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            if(args.length == 1){
                ServerManagerLogger.error("shutdown <Server name>");
            }
            if(args[1].equalsIgnoreCase("all")){
                for (String s : new HashSet<>(fileManager.getServerProcessesMap().keySet())) {
                    fileManager.shutdownServer(s);
                }
            }else{
                for (String s : new HashSet<>(fileManager.getServerProcessesMap().keySet())) {
                    if(s.startsWith(args[1])){
                        fileManager.shutdownServer(s);
                    }
                }
            }
        }
    }

    private class CommandDetach implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            for (ServerProcess process : new HashSet<>(fileManager.getServerProcessesMap().values())) {
                if(process.getScreen().isAttached()){
                    process.getScreen().detachFromScreen();
                }
            }
        }
    }

    private class CommandViewScreen implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            if(args.length == 0){
                help();
                return;
            }

            ServerProcess serverProcess = null;
            for (String s : new HashSet<>(fileManager.getServerProcessesMap().keySet())) {
                if(s.startsWith(args[1])) {
                    serverProcess = fileManager.getServerProcessesMap().get(s);
                    break;
                }
            }

            if(serverProcess == null){
                ServerManagerLogger.error("Could not find process '"+args[2]+"'");
            }

            if(serverProcess.getScreen().isAttached()){
                serverProcess.getScreen().detachFromScreen();
            }else{
                for (ServerProcess process : new HashSet<>(fileManager.getServerProcessesMap().values())) {
                    if(process.getScreen().isAttached()){
                        process.getScreen().detachFromScreen();
                    }
                }
                serverProcess.getScreen().attachToScreen();
            }
        }
    }

    private class CommandScreenList implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            if(args.length == 0 || !args[0].equals("-l")){
                help();
                return;
            }

            ServerManagerLogger.info(Attribute.CYAN_TEXT(), "Screens:");
            for (String name : new HashSet<>(fileManager.getServerProcessesMap().keySet())) {
                ServerManagerLogger.info(Attribute.MAGENTA_TEXT(), name);
            }
            ServerManagerLogger.info("Done");
        }
    }

    private class CommandDeleteUsedLogs implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            File file = new File("serverLogs");
            if(file.exists() && file.listFiles() != null){
                for (File f : file.listFiles()) {
                    if(!fileManager.getServerProcessesMap().keySet().contains(f.getName())){
                        Utils.delete(f);
                    }
                }
            }
            ServerManagerLogger.info("Done");
        }
    }

    private class CommandInsertCommand implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            if(args.length <= 2){
                ServerManagerLogger.error("insertCommand <Server name> <Cmd>");
            }else{
                ServerProcess serverProcess = null;
                for (String s : new HashSet<>(fileManager.getServerProcessesMap().keySet())) {
                    if(s.startsWith(args[1])) {
                        serverProcess = fileManager.getServerProcessesMap().get(s);
                        break;
                    }
                }
                if(serverProcess == null){
                    ServerManagerLogger.error("Server '"+args[1]+"' not found");
                }else{
                    StringBuilder stringBuilder = new StringBuilder(" ");
                    for (int i = 2; i < args.length; i++) {
                        stringBuilder.append(" ").append(args[i]);
                    }
                    String command = stringBuilder.toString().replace("  ", "");
                    Server server = ServerList.getByName(serverProcess.getName());
                    if(server == null){
                        ServerManagerLogger.error("Server '"+args[1]+"' is starting...");
                    }else{
                        server.sendPacket(new PacketExecuteConsoleCommand(command));
                        ServerManagerLogger.info("Command sent");
                    }
                }
            }
        }
    }

    private class CommandScale implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            if(fileManager.getAutoScaler() != null){
                fileManager.getAutoScaler().scale();
                ServerManagerLogger.info("Scaled");
            }
        }
    }

    private class CommandCreate implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            if(args.length == 1){
                ServerManagerLogger.error("create <Template Name>");
            }else{
                int count = 1;
                if(args.length == 3){
                    try{
                        count = Integer.valueOf(args[2]);
                    }catch (Exception e){
                        ServerManagerLogger.error("create <Template Name> <[count]>");
                        return;
                    }
                }
                for (int i = 0; i < count; i++) {
                    fileManager.createServer(args[1]);
                }
            }
        }
    }

    private class CommandReloadTemplates implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            ServerManagerLogger.info("Reloading...");
            fileManager.getServerManager().getConfigurationManager().loadTemplates();
            ServerManagerLogger.info("Templates reloaded");
        }
    }
}
