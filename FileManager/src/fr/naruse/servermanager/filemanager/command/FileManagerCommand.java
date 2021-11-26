package fr.naruse.servermanager.filemanager.command;

import com.diogonunes.jcolor.Attribute;
import fr.naruse.servermanager.core.command.AbstractCoreCommand;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.api.logging.GlobalLogger;
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
        super();
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
        this.registerCommand("complexScreen", new CommandComplexScreen());
    }

    @Override
    public void help() {
        GlobalLogger.info(Attribute.CYAN_TEXT(), "Available commands:");
        GlobalLogger.info("");
        GlobalLogger.info("-> stop (Stop server)");
        GlobalLogger.info("-> shutdown <Server name, All>");
        GlobalLogger.info("-> status");
        GlobalLogger.info("-> generateSecretKey");
        GlobalLogger.info("-> create <Template Name> <[count]>");
        GlobalLogger.info("-> scale");
        GlobalLogger.info("-> insertCommand <Server name> <Cmd>");
        GlobalLogger.info("-> deleteUnUsedLogs");
        GlobalLogger.info("-> screen -l (List all processes)");
        GlobalLogger.info("-> viewScreen <Process Name> (Attach on screen)");
        GlobalLogger.info("-> detach (Detach from current screen)");
        GlobalLogger.info("-> reloadTemplates");
        GlobalLogger.info("-> complexScreen <Process Name> (New window with logs | Only for graphical machines)");
        for (String pluginCommandUsage : this.pluginCommandUsages) {
            GlobalLogger.info("-> "+pluginCommandUsage);
        }
        GlobalLogger.info("");
    }

    private class CommandShutdown implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            if(args.length <= 1){
                GlobalLogger.error("shutdown <Server name>");
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
            if(args.length <= 1){
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
                GlobalLogger.error("Could not find process '"+args[1]+"'");
                return;
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
            if(args.length <= 1 || !args[0].equals("-l")){
                help();
                return;
            }

            GlobalLogger.info(Attribute.CYAN_TEXT(), "Screens:");
            for (String name : new HashSet<>(fileManager.getServerProcessesMap().keySet())) {
                GlobalLogger.info(Attribute.MAGENTA_TEXT(), name);
            }
            GlobalLogger.info("Done");
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
            GlobalLogger.info("Done");
        }
    }

    private class CommandInsertCommand implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            if(args.length <= 2){
                GlobalLogger.error("insertCommand <Server name> <Cmd>");
            }else{
                ServerProcess serverProcess = null;
                for (String s : new HashSet<>(fileManager.getServerProcessesMap().keySet())) {
                    if(s.startsWith(args[1])) {
                        serverProcess = fileManager.getServerProcessesMap().get(s);
                        break;
                    }
                }
                if(serverProcess == null){
                    GlobalLogger.error("Server '"+args[1]+"' not found");
                }else{
                    StringBuilder stringBuilder = new StringBuilder(" ");
                    for (int i = 2; i < args.length; i++) {
                        stringBuilder.append(" ").append(args[i]);
                    }
                    String command = stringBuilder.toString().replace("  ", "");
                    Server server = ServerList.getByName(serverProcess.getName());
                    if(server == null){
                        GlobalLogger.error("Server '"+args[1]+"' is starting...");
                    }else{
                        server.sendPacket(new PacketExecuteConsoleCommand(command));
                        GlobalLogger.info("Command sent");
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
                GlobalLogger.info("Scaled");
            }
        }
    }

    private class CommandCreate implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            if(args.length <= 1){
                GlobalLogger.error("create <Template Name>");
            }else{
                int count = 1;
                if(args.length == 3){
                    try{
                        count = Integer.valueOf(args[2]);
                    }catch (Exception e){
                        GlobalLogger.error("create <Template Name> <[count]>");
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
            GlobalLogger.info("Reloading...");
            fileManager.getServerManager().getConfigurationManager().loadTemplates();
            GlobalLogger.info("Templates reloaded");
        }
    }

    private class CommandComplexScreen implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            if(args.length <= 1){
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
                GlobalLogger.error("Could not find process '"+args[1]+"'");
                return;
            }

            serverProcess.getScreen().newWindow();
        }
    }
}
