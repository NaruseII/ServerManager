package fr.naruse.servermanager.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;
import fr.naruse.servermanager.core.connection.packet.PacketKickPlayer;
import fr.naruse.servermanager.core.connection.packet.PacketTeleportToPlayer;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

import java.util.Optional;

public class InventoryNukkiPlayer extends AbstractNukkitInventory {

    private Server currentServer;
    private final String owner;

    public InventoryNukkiPlayer(PluginBase pl, Player p, Server server, String owner) {
        super(pl, p, "§lPlayer '§c§l"+owner+"§r§l'", false);
        this.currentServer = server;
        this.owner = owner;

        initInventory(inventory);
        inventory.open(p);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        setDecoration();

        this.addServer(currentServer);

        inventory.addItem(buildItem(Item.COMPASS, 0, "§6Teleport", false, null));
        inventory.addItem(buildItem(Item.DIAMOND_SWORD, 0, "§4Kick", false, null));

        inventory.setItem(inventory.getSize()-1, buildItem(Item.IRON_BAR, 0, "§cBack", false, null));
    }

    @Override
    protected void actionPerformed(Player p, Item item, int slot) {
        if(ServerList.getByName(currentServer.getName()) == null){
            if(ServerList.isPlayerOnline(owner)){
                Optional<Server> optional = ServerList.findPlayerBukkitOrSpongeServer(owner);
                if(optional.isPresent()){
                    currentServer = optional.get();
                    inventory.clearAll();
                    initInventory(inventory);
                }else{
                    new InventoryNukkitServerList(pl, p);
                    return;
                }
            }
        }

        if(item != null && item.getId() != Item.STAINED_GLASS_PANE){
            if(item.getId() == Item.COMPASS){
                Optional<Server> optionalProxy = ServerList.findPlayerProxyServer(p.getName());
                if(optionalProxy.isPresent()){
                    optionalProxy.get().sendPacket(new PacketTeleportToPlayer(p.getName(), owner));
                }else{
                    inventory.close(p);
                    p.sendMessage("§cNo Proxy found.");
                }
            }else if(item.getId() == Item.DIAMOND_SWORD){
                Optional<Server> optionalProxy = ServerList.findPlayerProxyServer(p.getName());
                if(optionalProxy.isPresent()){
                    optionalProxy.get().sendPacket(new PacketKickPlayer(owner));
                }else{
                    Optional<Server> optionalServer = ServerList.findPlayerBukkitOrSpongeServer(owner);
                    if(optionalServer.isPresent()){
                        optionalServer.get().sendPacket(new PacketKickPlayer(owner));
                    }else{
                        inventory.close(p);
                        p.sendMessage("§cPlayer's server not found.");
                        return;
                    }
                }
                slot = inventory.getSize()-1;
            }else{
                Server server = getServerFromItem(item);

                if(server != null){
                    new InventoryNukkitServer(pl, p, server);
                    return;
                }else{
                    inventory.clearAll();
                    initInventory(inventory);
                }
            }
        }

        if(slot == inventory.getSize()-1){
            new InventoryNukkitServer(pl, p, currentServer);
        }
    }
}
