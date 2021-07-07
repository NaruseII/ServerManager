package fr.naruse.servermanager.bukkit.inventory;

import fr.naruse.servermanager.core.connection.packet.PacketKickPlayer;
import fr.naruse.servermanager.core.connection.packet.PacketTeleportToPlayer;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class InventoryPlayer extends AbstractInventory{

    private Server currentServer;
    private final String owner;

    public InventoryPlayer(JavaPlugin pl, Player p, Server server, String owner) {
        super(pl, p, "§lPlayer '§c§l"+owner+"§r§l'", 9*6, false);
        this.currentServer = server;
        this.owner = owner;

        initInventory(inventory);
        p.openInventory(inventory);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        setDecoration();

        this.addServer(currentServer);

        inventory.addItem(buildItem(Material.COMPASS, 0, "§6Teleport", false, null));
        inventory.addItem(buildItem(Material.DIAMOND_SWORD, 0, "§4Kick", false, null));

        inventory.setItem(inventory.getSize()-1, buildItem(Material.BARRIER, 0, "§cBack", false, null));
    }

    @Override
    protected void actionPerformed(Player p, ItemStack item, InventoryAction action, int slot) {
        if(ServerList.getByName(currentServer.getName()) == null){
            if(ServerList.isPlayerOnline(owner)){
                Optional<Server> optional = ServerList.findPlayerBukkitOrSpongeServer(owner);
                if(optional.isPresent()){
                    currentServer = optional.get();
                    inventory.clear();
                    initInventory(inventory);
                }else{
                    new InventoryServerList(pl, p);
                    return;
                }
            }
        }

        if(item != null && item.getType() != Material.STAINED_GLASS_PANE){
            if(item.getType() == Material.COMPASS){
                Optional<Server> optionalProxy = ServerList.findPlayerProxyServer(p.getName());
                if(optionalProxy.isPresent()){
                    optionalProxy.get().sendPacket(new PacketTeleportToPlayer(p.getName(), owner));
                }else{
                    p.closeInventory();
                    p.sendMessage("§cNo Proxy found.");
                }
            }else if(item.getType() == Material.DIAMOND_SWORD){
                Optional<Server> optionalProxy = ServerList.findPlayerProxyServer(p.getName());
                if(optionalProxy.isPresent()){
                    optionalProxy.get().sendPacket(new PacketKickPlayer(owner));
                }else{
                    Optional<Server> optionalServer = ServerList.findPlayerBukkitOrSpongeServer(owner);
                    if(optionalServer.isPresent()){
                        optionalServer.get().sendPacket(new PacketKickPlayer(owner));
                    }else{
                        p.closeInventory();
                        p.sendMessage("§cPlayer's server not found.");
                        return;
                    }
                }
                slot = inventory.getSize()-1;
            }else{
                Server server = getServerFromItem(item);

                if(server != null){
                    new InventoryServer(pl, p, server);
                    return;
                }else{
                    inventory.clear();
                    initInventory(inventory);
                }
            }
        }

        if(slot == inventory.getSize()-1){
            new InventoryServer(pl, p, currentServer);
        }
    }
}
