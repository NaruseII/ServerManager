package fr.naruse.servermanager.sponge.cmd;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.connection.packet.PacketCreateServer;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SpongeServerManagerCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(Text.of("§6/§7sm createServer <Template name>"));
        src.sendMessage(Text.of( "§6/§7sm shutdown <Server name, -All>"));
        src.sendMessage(Text.of( "§6/§7sm insertCommand <Server name> <Command>"));
        src.sendMessage(Text.of( "§6/§7sm status <-ls>"));
        return CommandResult.success();
    }

    public static List<String> completeServerName(String arg){
        return ServerList.getAll().stream().filter(server -> server.getName().startsWith(arg)).map(server -> server.getName()).collect(Collectors.toList());
    }

    public static class CreateServer implements CommandCallable {
        @Override
        public CommandResult process(CommandSource source, String arguments) throws CommandException {
            String[] args = arguments.split(" ");

            if(arguments.isEmpty() || args.length == 0){
                source.sendMessage(Text.of("§6/§7sm createServer <Template name>"));
            }else{
                ServerManager.get().getConnectionManager().sendPacket(new PacketCreateServer(args[0]));
                source.sendMessage(Text.of("§aCreation packet sent."));
            }

            return CommandResult.success();
        }

        @Override
        public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
            return Collections.emptyList();
        }

        @Override
        public boolean testPermission(CommandSource source) {
            return true;
        }

        @Override
        public Optional<Text> getShortDescription(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Optional<Text> getHelp(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Text getUsage(CommandSource source) {
            return Text.of("<Template name>");
        }
    }

    public static class ShutdownServer implements CommandCallable {
        @Override
        public CommandResult process(CommandSource source, String arguments) throws CommandException {
            String[] args = arguments.split(" ");

            if(arguments.isEmpty() || args.length == 0){
                source.sendMessage(Text.of( "§6/§7sm shutdown <Server name, -All>"));
            }else{
                Set<Server> set = ServerList.findServer(CoreServerType.BUKKIT_MANAGER, CoreServerType.BUNGEE_MANAGER, CoreServerType.VELOCITY_MANAGER, CoreServerType.SPONGE_MANAGER);
                if(!args[0].equalsIgnoreCase("-all")){
                    set = set.stream().filter(server -> server.getName().startsWith(args[0])).collect(Collectors.toSet());
                }
                set.forEach(server -> server.sendPacket(new PacketShutdown()));

                source.sendMessage(Text.of("§a"+set.size()+" server stopped."));
            }

            return CommandResult.success();
        }

        @Override
        public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
            return completeServerName(arguments);
        }

        @Override
        public boolean testPermission(CommandSource source) {
            return true;
        }

        @Override
        public Optional<Text> getShortDescription(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Optional<Text> getHelp(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Text getUsage(CommandSource source) {
            return Text.of("<Server name, -All>");
        }
    }

    public static class InsertCommand implements CommandCallable {
        @Override
        public CommandResult process(CommandSource source, String arguments) throws CommandException {
            String[] args = arguments.split(" ");

            if(arguments.isEmpty() || args.length < 1){
                source.sendMessage(Text.of( "§6/§7sm insertCommand <Server name> <Command>"));
            }else{
                Server server = ServerList.getByName(args[0]);
                if(server == null){
                    source.sendMessage(Text.of("§cServer '"+args[0]+"' not found."));
                }else{
                    StringBuilder stringBuilder = new StringBuilder(" ");
                    for (int i = 1; i < args.length; i++) {
                        stringBuilder.append(" ").append(args[i]);
                    }

                    String command = stringBuilder.toString().replace("  ", "");

                    server.sendPacket(new PacketExecuteConsoleCommand(command));

                    source.sendMessage(Text.of("§aCommand '"+command+"' inserted into '"+server.getName()+"'."));
                }
            }

            return CommandResult.success();
        }

        @Override
        public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
            return completeServerName(arguments);
        }

        @Override
        public boolean testPermission(CommandSource source) {
            return true;
        }

        @Override
        public Optional<Text> getShortDescription(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Optional<Text> getHelp(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Text getUsage(CommandSource source) {
            return Text.of("<Server name> <Command>");
        }
    }

    public static class Status implements CommandCallable {

        @Override
        public CommandResult process(CommandSource source, String arguments) throws CommandException {
            String[] args = arguments.split(" ");

            source.sendMessage(Text.of("§aServer list:"));
            boolean ls = false;
            if(!arguments.isEmpty()){
                ls = args[0].equalsIgnoreCase("-ls");
            }
            for (Server server : ServerList.getAll()) {
                source.sendMessage(Text.of(""));
                source.sendMessage(Text.of(" §3-> §b"+server.getName()+" §7[§6"+server.getCoreServerType()+"§7]"));
                if(ls){
                    source.sendMessage(Text.of("    §5Port: §d"+server.getPort()));
                    source.sendMessage(Text.of("    §5ServerManagerPort: §d"+server.getServerManagerPort()));
                }
                source.sendMessage(Text.of("    §5Capacity: §d"+server.getData().getCapacity()));
                source.sendMessage(Text.of("    §5PlayerSize: §d"+server.getData().getPlayerSize()));
                source.sendMessage(Text.of("    §5Players: §d"+(ls ? server.getData().getUUIDByNameMap().toString() : server.getData().getUUIDByNameMap().keySet().toString())));
                source.sendMessage(Text.of("    §5Status: §d"+server.getData().getStatusSet().toString()));
            }

            return CommandResult.success();
        }

        @Override
        public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
            return Collections.emptyList();
        }

        @Override
        public boolean testPermission(CommandSource source) {
            return true;
        }

        @Override
        public Optional<Text> getShortDescription(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Optional<Text> getHelp(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Text getUsage(CommandSource source) {
            return Text.of("<-ls>");
        }
    }
}
