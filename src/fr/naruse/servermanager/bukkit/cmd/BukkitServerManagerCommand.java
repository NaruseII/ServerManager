package fr.naruse.servermanager.bukkit.cmd;

import fr.naruse.servermanager.bukkit.main.BukkitManagerPlugin;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.connection.packet.PacketCreateServer;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BukkitServerManagerCommand implements CommandExecutor, TabCompleter {

    private final BukkitManagerPlugin pl;

    public BukkitServerManagerCommand(BukkitManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(!sender.hasPermission("servermanager")){
            return sendMessage(sender, "§4You do not have this permission.");
        }

        if(args.length == 0){
            this.sendMessage(sender, "§6/§7sm createServer <Template name>");
            this.sendMessage(sender, "§6/§7sm shutdown <Server name, -All>");
            this.sendMessage(sender, "§6/§7sm insertCommand <Server name> <Command>");
            return this.sendMessage(sender, "§6/§7sm status <-ls>");
        }

        // STATUS
        if(args[0].equalsIgnoreCase("status")){
            this.sendMessage(sender, "§aServer list:");
            boolean ls = false;
            if(args.length == 2){
                ls = args[1].equalsIgnoreCase("-ls");
            }
            for (Server server : ServerList.getAll()) {
                this.sendMessage(sender, "");
                this.sendMessage(sender, " §3-> §b"+server.getName()+" §7[§6"+server.getCoreServerType()+"§7]");
                if(ls){
                    this.sendMessage(sender, "    §5Port: §d"+server.getPort());
                    this.sendMessage(sender, "    §5ServerManagerPort: §d"+server.getServerManagerPort());
                }
                this.sendMessage(sender, "    §5Capacity: §d"+server.getData().getCapacity());
                this.sendMessage(sender, "    §5PlayerSize: §d"+server.getData().getPlayerSize());
                this.sendMessage(sender, "    §5Players: §d"+(ls ? server.getData().getUUIDByNameMap().toString() : server.getData().getUUIDByNameMap().keySet().toString()));
                this.sendMessage(sender, "    §5Status: §d"+server.getData().getStatusSet().toString());
            }
            return true;
        }

        // CREATE SERVER
        if(args[0].equalsIgnoreCase("createServer")){
            if(args.length != 2){
                return this.sendMessage(sender, "§6/§7sm createServer <Template name>");
            }
            String templateName = args[1];
            this.pl.getServerManager().getConnectionManager().sendPacket(new PacketCreateServer(templateName));
            return this.sendMessage(sender, "§aCreation packet sent.");
        }

        // SHUTDOWN
        if(args[0].equalsIgnoreCase("shutdown")){
            if(args.length != 2){
                return this.sendMessage(sender, "§6/§7sm shutdown <Server, -All>");
            }
            Set<Server> set = ServerList.findServer(CoreServerType.BUKKIT_MANAGER, CoreServerType.BUNGEE_MANAGER);
            if(!args[1].equalsIgnoreCase("-all")){
                set = set.stream().filter(server -> server.getName().startsWith(args[1])).collect(Collectors.toSet());
            }
            set.forEach(server -> server.sendPacket(new PacketShutdown()));

            return this.sendMessage(sender, "§a"+set.size()+" server stopped.");
        }

        // INSERT COMMAND
        if(args[0].equalsIgnoreCase("insertCommand")){
            if(args.length < 3){
                return this.sendMessage(sender, "§6/§7sm insertCommand <Server name> <Command>");
            }

            Server server = ServerList.getByName(args[1]);
            if(server == null){
                return this.sendMessage(sender, "§cServer '"+args[1]+"' not found.");
            }

            StringBuilder stringBuilder = new StringBuilder(" ");
            for (int i = 2; i < args.length; i++) {
                stringBuilder.append(" ").append(args[i]);
            }

            String command = stringBuilder.toString().replace("  ", "");

            server.sendPacket(new PacketExecuteConsoleCommand(command));

            return this.sendMessage(sender, "§aCommand '"+command+"' inserted into '"+server.getName()+"'.");
        }
        return false;
    }

    private boolean sendMessage(CommandSender sender, String msg){
        sender.sendMessage(msg);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        return ServerList.getAll().stream().filter(server -> server.getName().startsWith(args[args.length-1])).map(server -> server.getName()).collect(Collectors.toList());
    }
}
