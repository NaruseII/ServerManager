package fr.naruse.servermanager.sponge.packet;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.sponge.main.SpongeManagerPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SpongeProcessPacketListener extends ProcessPacketListener {

    private SpongeManagerPlugin pl;

    public SpongeProcessPacketListener(SpongeManagerPlugin pl) {
        this.pl = pl;
    }

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), packet.getCommand());
    }

    @Override
    public void processBroadcast(PacketBroadcast packet) {
        Sponge.getServer().getBroadcastChannel().send(Text.of(packet.getMessage()));
    }

    @Override
    public void processTeleportToLocation(PacketTeleportToLocation packet) {
        SpongeExecutorService service = Sponge.getScheduler().createSyncExecutor(pl);
        service.scheduleAtFixedRate(new Runnable() {
            int i = 0;
            @Override
            public void run() {
                if(i >= 50){
                    service.shutdown();
                    return;
                }else {
                    i++;
                }
                Optional<Player> optional = Sponge.getServer().getPlayer(packet.getPlayerName());
                if(optional.isPresent()){
                    World world = null;
                    try{
                        int index = Integer.valueOf(packet.getWorldName());
                        int i = 0;
                        for (World w : Sponge.getServer().getWorlds()) {
                            if (i == index){
                                world = w;
                                break;
                            }else{
                                i++;
                            }
                        }
                    }catch (Exception e){
                        world = Sponge.getServer().getWorld(packet.getWorldName()).get();
                    }
                    if(world == null){
                        GlobalLogger.error("Something tried to teleport '"+packet.getPlayerName()+"' to the world '"+packet.getWorldName()+"' but doesn't exist!");
                        service.shutdown();
                        return;
                    }
                    Location<World> location = new Location(world, packet.getX(), packet.getY(), packet.getZ());
                    optional.get().setLocationAndRotationSafely(location, new Vector3d(new Vector2d(packet.getYaw(), packet.getPitch())));
                    service.shutdown();
                }
            }
        }, 0, 250, TimeUnit.MILLISECONDS);
    }

    @Override
    public void processTeleportToPlayer(PacketTeleportToPlayer packet) {
        SpongeExecutorService service = Sponge.getScheduler().createSyncExecutor(pl);
        service.scheduleAtFixedRate(new Runnable() {
            int i = 0;
            @Override
            public void run() {
                if(i >= 50){
                    service.shutdown();
                    return;
                }else {
                    i++;
                }
                Optional<Player> optional = Sponge.getServer().getPlayer(packet.getPlayerName());
                Optional<Player> optionalTarget = Sponge.getServer().getPlayer(packet.getTargetName());
                if(optional.isPresent() && optionalTarget.isPresent()){
                    optional.get().setLocationAndRotationSafely(optionalTarget.get().getLocation(), optionalTarget.get().getHeadRotation());
                    service.shutdown();
                }
            }
        }, 0, 250, TimeUnit.MILLISECONDS);
    }

    @Override
    public void processKickPlayer(PacketKickPlayer packet) {
        SpongeExecutorService service = Sponge.getScheduler().createSyncExecutor(pl);
        service.submit(() -> {
            Optional<Player> optional = Sponge.getServer().getPlayer(packet.getPlayerName());
            if(optional.isPresent()){
                optional.get().kick(Text.of(packet.getReason() == null ? "" : packet.getReason()));
            }
        });
        service.shutdown();
    }
}
