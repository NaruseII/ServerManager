package fr.naruse.servermanager.nukkit.packet;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.scheduler.Task;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.nukkit.main.NukkitManagerPlugin;

public class NukkitProcessPacketListener extends ProcessPacketListener {

    private final NukkitManagerPlugin pl;

    public NukkitProcessPacketListener(NukkitManagerPlugin nukkitManagerPlugin) {
        this.pl = nukkitManagerPlugin;
    }

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        this.pl.getServer().dispatchCommand(this.pl.getServer().getConsoleSender(), packet.getCommand());
    }

    @Override
    public void processBroadcast(PacketBroadcast packet) {
        this.pl.getServer().broadcastMessage(packet.getMessage());
    }

    @Override
    public void processTeleportToLocation(PacketTeleportToLocation packet) {
        this.pl.getServer().getScheduler().scheduleRepeatingTask(new Task() {

            int i = 0;

            @Override
            public void onRun(int o) {
                if(i >= 50){
                    cancel();
                }else{
                    i++;

                    Player player = pl.getServer().getPlayer(packet.getPlayerName());
                    if(player != null){

                        Level world;
                        try{
                            world = pl.getServer().getLevel(Integer.valueOf(packet.getWorldName()));
                        }catch (Exception e){
                            world = pl.getServer().getLevelByName(packet.getWorldName());
                        }
                        if(world == null){
                            ServerManagerLogger.error("Something tried to teleport '"+packet.getPlayerName()+"' to the world '"+packet.getWorldName()+"' but doesn't exist!");
                            cancel();
                            return;
                        }
                        Location location = new Location(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), world);
                        player.teleport(location);
                        cancel();
                    }
                }
            }
        }, 5);
    }

    @Override
    public void processTeleportToPlayer(PacketTeleportToPlayer packet) {
        this.pl.getServer().getScheduler().scheduleRepeatingTask(new Task() {

            int i = 0;

            @Override
            public void onRun(int o) {
                if(i >= 50){
                    cancel();
                }else{
                    i++;

                    Player player = pl.getServer().getPlayer(packet.getPlayerName());
                    Player target = pl.getServer().getPlayer(packet.getTargetName());
                    if(player != null && target != null){
                        player.teleport(target);
                        cancel();
                    }
                }
            }
        }, 5);
    }

    @Override
    public void processKickPlayer(PacketKickPlayer packet) {
        Player player = pl.getServer().getPlayer(packet.getPlayerName());
        if(player != null){
            player.kick(packet.getReason());
        }
    }
}
