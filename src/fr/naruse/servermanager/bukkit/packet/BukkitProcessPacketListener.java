package fr.naruse.servermanager.bukkit.packet;

import fr.naruse.servermanager.bukkit.main.BukkitManagerPlugin;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitProcessPacketListener extends ProcessPacketListener {

    private final BukkitManagerPlugin pl;

    public BukkitProcessPacketListener(BukkitManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        Bukkit.getScheduler().runTask(pl, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), packet.getCommand()));
    }

    @Override
    public void processBroadcast(PacketBroadcast packet) {
        Bukkit.broadcastMessage(packet.getMessage());
    }

    @Override
    public void processTeleportToLocation(PacketTeleportToLocation packet) {
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if(i >= 50){
                    cancel();
                }else{
                    i++;

                    Player player = Bukkit.getPlayer(packet.getPlayerName());
                    if(player != null){

                        World world;
                        try{
                            world = Bukkit.getWorlds().get(Integer.valueOf(packet.getWorldName()));
                        }catch (Exception e){
                            world = Bukkit.getWorld(packet.getWorldName());
                        }
                        if(world == null){
                            ServerManagerLogger.error("Something tried to teleport '"+packet.getPlayerName()+"' to the world '"+packet.getWorldName()+"' but doesn't exist!");
                            cancel();
                            return;
                        }
                        Location location = new Location(world, packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
                        player.teleport(location);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(pl, 5, 5);
    }

    @Override
    public void processTeleportToPlayer(PacketTeleportToPlayer packet) {
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if(i >= 50){
                    cancel();
                }else{
                    i++;

                    Player player = Bukkit.getPlayer(packet.getPlayerName());
                    Player target = Bukkit.getPlayer(packet.getTargetName());
                    if(player != null && target != null){
                        player.teleport(target);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(pl, 0, 5);
    }

    @Override
    public void processKickPlayer(PacketKickPlayer packet) {
        Bukkit.getScheduler().runTask(pl, () -> {
            Player p = Bukkit.getPlayer(packet.getPlayerName());
            if(p != null){
                p.kickPlayer(packet.getReason() == null ? "" : packet.getReason());
            }
        });
    }
}
