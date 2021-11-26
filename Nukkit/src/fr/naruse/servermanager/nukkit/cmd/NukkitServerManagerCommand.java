package fr.naruse.servermanager.nukkit.cmd;

import cn.nukkit.command.CommandSender;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.connection.packet.PacketCreateServer;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.nukkit.main.NukkitManagerPlugin;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NukkitServerManagerCommand {

    public static void onCommand(NukkitManagerPlugin pl, CommandSender sender, String[] args) {
        if(!sender.hasPermission("servermanager")){
            sender.sendMessage("§4You do not have this permission.");
            return;
        }

        if(args.length == 0){
            sender.sendMessage("§6/§7sm createServer <Template name> <[File-Manager]>");
            sender.sendMessage("§6/§7sm shutdown <Server name, -All>");
            sender.sendMessage("§6/§7sm insertCommand <Server name> <Command>");
            sender.sendMessage("§6/§7sm status <-ls>");
            return;
        }

        // STATUS
        if(args[0].equalsIgnoreCase("status")){
            sender.sendMessage("§aServer list:");

            boolean ls = false;
            if(args.length == 2){
                ls = args[1].equalsIgnoreCase("-ls");
            }
            for (Server server : ServerList.getAll()) {
                sender.sendMessage("");
                sender.sendMessage(" §3-> §b"+server.getName()+" §7[§6"+server.getCoreServerType()+"§7]");
                if(ls){
                    sender.sendMessage("    §5Port: §d"+server.getPort());
                    sender.sendMessage("    §5ServerManagerPort: §d"+server.getServerManagerPort());
                }
                sender.sendMessage("    §5Capacity: §d"+server.getData().getCapacity());
                sender.sendMessage("    §5PlayerSize: §d"+server.getData().getPlayerSize());
                sender.sendMessage("    §5Players: §d"+(ls ? server.getData().getUUIDByNameMap().toString() : server.getData().getUUIDByNameMap().keySet().toString()));
                sender.sendMessage("    §5Status: §d"+server.getData().getStatusSet().toString());
            }
            return;
        }

        // CREATE SERVER
        if(args[0].equalsIgnoreCase("createServer")){
            if(args.length < 2){
                sender.sendMessage("§6/§7sm createServer <Template name> <[File-Manager]>");
                return;
            }
            PacketCreateServer packet = new PacketCreateServer(args[1]);
            if(args.length > 2){
                Optional<Server> optionalServer = ServerList.getByNameOptional(args[2]);
                if(optionalServer.isPresent()){
                    optionalServer.get().sendPacket(packet);
                }else{
                    sender.sendMessage("§cServer '"+args[2]+"' not found.");
                    return;
                }
            }else{
                pl.getServerManager().getConnectionManager().sendPacket(packet);
            }
            sender.sendMessage("§aCreation packet sent.");
            return;
        }

        // SHUTDOWN
        if(args[0].equalsIgnoreCase("shutdown")){
            if(args.length != 2){
                sender.sendMessage("§6/§7sm shutdown <Server, -All>");
                return;
            }
            Set<Server> set = ServerList.findServer(CoreServerType.BUKKIT_MANAGER, CoreServerType.BUNGEE_MANAGER, CoreServerType.VELOCITY_MANAGER, CoreServerType.SPONGE_MANAGER, CoreServerType.NUKKIT_MANAGER);
            if(!args[1].equalsIgnoreCase("-all")){
                set = set.stream().filter(server -> server.getName().startsWith(args[1])).collect(Collectors.toSet());
            }
            set.forEach(server -> server.sendPacket(new PacketShutdown()));

            sender.sendMessage("§a"+set.size()+" server stopped.");
            return;
        }

        // INSERT COMMAND
        if(args[0].equalsIgnoreCase("insertCommand")){
            if(args.length < 3){
                sender.sendMessage("§6/§7sm insertCommand <Server name> <Command>");
                return;
            }

            Server server = ServerList.getByName(args[1]);
            if(server == null){
                sender.sendMessage("§cServer '"+args[1]+"' not found.");
                return;
            }

            StringBuilder stringBuilder = new StringBuilder(" ");
            for (int i = 2; i < args.length; i++) {
                stringBuilder.append(" ").append(args[i]);
            }

            String command = stringBuilder.toString().replace("  ", "");

            server.sendPacket(new PacketExecuteConsoleCommand(command));

            sender.sendMessage("§aCommand '"+command+"' inserted into '"+server.getName()+"'.");
        }
    }
}
